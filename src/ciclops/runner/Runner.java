package ciclops.runner;

import ciclops.credentials.AbstractUsernamePasswordCredentials;
import ciclops.credentials.DockerRepoCredentials;
import ciclops.credentials.NyxCredentials;
import ciclops.credentials.fileGenerators.DockerLoginFileGenerator;
import ciclops.credentials.service.CredentialsService;
import ciclops.projects.Project;
import ciclops.projects.service.ProjectsService;
import ciclops.runner.service.BuildProcessLogService;
import common.logger.Logger;
import dobby.util.json.NewJson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;

public class Runner {
    private final Logger LOGGER = new Logger(Runner.class);
    private final UUID id;
    private final UUID projectId;
    private final boolean isRelease;
    private static final String BUILD_POD_IMAGE = "ciclopsbuilder:0.49";
    private static final CredentialsService credentialsService = CredentialsService.getInstance();
    private final List<String> additionalMounts = new ArrayList<>();
    private final Map<String, String> additionalEnv = new HashMap<>();

    public Runner(UUID id, UUID projectId) {
        this(id, projectId, false);
    }

    public Runner(UUID id, UUID projectId, boolean isRelease) {
        this.id = id;
        this.projectId = projectId;
        this.isRelease = isRelease;
    }

    public void start() {
        LOGGER.debug("Starting runner " + id);
        final Project project = ProjectsService.getInstance().findById(projectId.toString());

        if (project == null) {
            LOGGER.warn("Project not found: " + projectId + ". Aborting build.");
            return;
        }

        final String scmUrl = project.getGitUrl();
        if (!createTmpDir()) {
            LOGGER.error("Failed to create tmp dir for project " + project.getName());
            return;
        }

        final AbstractUsernamePasswordCredentials[] credentials = getCredentials(project);

        if (!generateDockerLogin(credentials) || !setNyxCredentials(credentials)) {
            return;
        }

        additionalEnv.put("RELEASE", String.valueOf(isRelease));
        initBuildPod(scmUrl);
        cleanup();
    }

    private boolean generateDockerLogin(AbstractUsernamePasswordCredentials[] credentials) {
        final List<DockerRepoCredentials> dockerCredentials = Arrays.stream(credentials).filter(e -> e instanceof DockerRepoCredentials).map(e -> (DockerRepoCredentials) e).toList();

        if (dockerCredentials.isEmpty()) {
            LOGGER.debug("No docker credentials found for project " + projectId);
            return true;
        }

        for (DockerRepoCredentials dockerCredential : dockerCredentials) {
            if (
                    dockerCredential.getHost() == null || dockerCredential.getHost().isEmpty() ||
                    dockerCredential.getUsername() == null || dockerCredential.getUsername().isEmpty() ||
                    dockerCredential.getPassword() == null || dockerCredential.getPassword().isEmpty()
            ) {
                LOGGER.error("invalid docker credentials for project  " + projectId);
                return false;
            }

            final DockerRepoCredentials decryptedCredential = (DockerRepoCredentials) credentialsService.decryptCredentials(dockerCredential);
            if (decryptedCredential == null) {
                LOGGER.error("Failed to decrypt docker credentials for project " + projectId);
                return false;
            }

            additionalEnv.put("DOCKER_" + decryptedCredential.getHost().toUpperCase() + "_USERNAME", dockerCredential.getUsername());
            additionalEnv.put("DOCKER_" + decryptedCredential.getHost().toUpperCase() + "_PASSWORD", dockerCredential.getPassword());
        }

        final DockerLoginFileGenerator dockerLoginFileGenerator = new DockerLoginFileGenerator(dockerCredentials);
        additionalMounts.add(dockerLoginFileGenerator.getFileName() + ":" + dockerLoginFileGenerator.getContainerFilePath());

        if (!writeAuthFile(dockerLoginFileGenerator.getFileContent(), dockerLoginFileGenerator.getFileName())) {
            cleanup();
            LOGGER.error("Failed to write docker auth file for project " + projectId);
            return false;
        }

        return true;
    }

    private boolean setNyxCredentials(AbstractUsernamePasswordCredentials[] credentials) {
        final List<NyxCredentials> nyxCredentials = Arrays.stream(credentials).filter(e -> e instanceof NyxCredentials).map(e -> (NyxCredentials) e).toList();

        if (nyxCredentials.isEmpty()) {
            LOGGER.debug("No nyx credentials found for project " + projectId);
            return true;
        }

        for (NyxCredentials nyxCredential : nyxCredentials) {
            if (
                    nyxCredential.getHost() == null || nyxCredential.getHost().isEmpty() ||
                    nyxCredential.getPassword() == null || nyxCredential.getPassword().isEmpty()
            ) {
                LOGGER.error("invalid nyx credentials for project " + projectId);
                return false;
            }

            final NyxCredentials decryptedCredential = (NyxCredentials) credentialsService.decryptCredentials(nyxCredential);
            if (decryptedCredential == null) {
                LOGGER.error("Failed to decrypt nyx credentials for project " + projectId);
                return false;
            }

            additionalEnv.put("NYX_" + nyxCredential.getHost().toUpperCase() + "_PASSWORD", decryptedCredential.getPassword());
        }
        return true;
    }

    final AbstractUsernamePasswordCredentials[] getCredentials(Project project) {
        final List<String> credentialsIds = project.getCredentials();
        final List<AbstractUsernamePasswordCredentials> credentials = new ArrayList<>();

        for (String credentialsId : credentialsIds) {
            final AbstractUsernamePasswordCredentials credential = credentialsService.getCredentials(credentialsId);
            if (credential != null) {
                credentials.add(credential);
            } else {
                LOGGER.warn("Credentials not found: " + credentialsId);
            }
        }
        return credentials.toArray(new AbstractUsernamePasswordCredentials[0]);
    }

    private boolean writeAuthFile(String content, String fileName) {
        final String authFilePath = "/tmp/ciclops/" + id + "/" + fileName;
        try {
            final File authFile = new File(authFilePath);
            authFile.createNewFile();
            Files.writeString(authFile.toPath(), content);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to write auth file: " + authFilePath);
            LOGGER.trace(e);
            return false;
        }
    }

    private boolean createTmpDir() {
        final String tmpDir = "/tmp/ciclops/" + id;
        final File tmpDirFile = new File(tmpDir);

        if (tmpDirFile.exists()) {
            return true;
        }
        return tmpDirFile.mkdirs();
    }

    private void cleanup() {
        final String tmpDir = "/tmp/ciclops/" + id;
        final File tmpDirFile = new File(tmpDir);
        if (!tmpDirFile.exists()) {
            return;
        }
        try {
            Files.walk(tmpDirFile.toPath()).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    LOGGER.error("Failed to delete file: " + path);
                    LOGGER.trace(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to delete tmp dir: " + tmpDir);
            LOGGER.trace(e);
        }
    }

    private String getMounts() {
        final StringBuilder mounts = new StringBuilder();
        for (String mount : additionalMounts) {
            mounts.append("-v /tmp/ciclops/").append(id).append("/").append(mount).append(":ro ");
        }

        mounts
                .append("-v ")
                .append(System.getProperty("user.home"))
                .append("/.ciclops_ssh:/root/.ssh:ro ");

        return mounts.toString();
    }

    private String getEnv() {
        final StringBuilder env = new StringBuilder();
        for (Map.Entry<String, String> entry : additionalEnv.entrySet()) {
            env.append("-e ").append(entry.getKey().replace(".", "_")).append("=\"").append(entry.getValue()).append("\" ");
        }
        return env.toString();
    }

    private void initBuildPod(String scmUrl) {
        // TODO
        // - inject git credentials
        final String separator = "---" + id + "---";
        additionalEnv.put("SCM_URL", scmUrl);
        additionalEnv.put("SEPARATOR", separator);

        final String command = "podman run " + getEnv() + "--privileged " + getMounts() + "--rm " + BUILD_POD_IMAGE;
        final NewJson processLog = new NewJson();
        processLog.setString("id", id.toString());

        final List<String> output = new ArrayList<>();
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line
                        .replace("\u001B[0m", "")
                        .replace("\u001B[31m", "")
                        .replace("\u001B[32m", "")
                        .replace("\u001B[33m", "")
                        .replace("\u001B[34m", "")
                        .replace("\"", "'")
                );
                LOGGER.debug(line);
            }

            int exitCode = process.waitFor();
            processLog.setBoolean("success", findPipelineExitCode(output) == 0);
            if (exitCode != 0) {
                processLog.setBoolean("success", false);
                LOGGER.error("Failed to execute command \"" + command + "\". Exit code: " + exitCode);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing command: " + command);
            LOGGER.trace(e);
            processLog.setBoolean("success", false);
            processLog.setString("error", e.getMessage());
        }
        processLog.setList("rawOutput", output.stream().map(o -> (Object) o).toList());
        try {
            splitIntoSteps(processLog, output);
        } catch (Exception e) {
            LOGGER.error("Failed to split logs into steps");
            LOGGER.trace(e);
            processLog.setList("steps", List.of());
        }

        if (!BuildProcessLogService.getInstance().saveLog(id, projectId, processLog)) {
            LOGGER.error("Failed to save build log for project " + projectId);
        } else {
            LOGGER.debug("Build log saved successfully for project " + projectId);
        }

        if (!BuildProcessLogService.getInstance().addLogLastRuns(projectId, processLog)) {
            LOGGER.error("Failed to add build log to last runs for project " + projectId);
        } else {
            LOGGER.debug("Build log added to last runs successfully for project " + projectId);
        }
    }

    private int findPipelineExitCode(List<String> output) {
        for (String line : output.reversed()) {
            if (line.contains("|CICLOPS_EXIT_CODE:")) {
                final String exitCode = line.substring(line.indexOf("|CICLOPS_EXIT_CODE:") + "|CICLOPS_EXIT_CODE:".length()).trim();
                try {
                    return Integer.parseInt(exitCode);
                } catch (NumberFormatException e) {
                    LOGGER.error("Failed to parse exit code: " + exitCode);
                    return -1;
                }
            }
        }
        return 0;
    }

    private void splitIntoSteps(NewJson pipelineLog, List<String> log) {
        final List<String> processedLogs = log.stream().map(l -> l.substring(l.indexOf("|") + 1)).toList();
        final List<NewJson> steps = new ArrayList<>();

        String currentStep = "";
        List<String> buffer = new ArrayList<>();

        for (String line : processedLogs) {
            if (line.contains("---" + id + "---")) {
                buffer = buffer.stream().filter(l -> !l.matches("CICLOPS_EXIT_CODE:.*")).toList();
                if (!buffer.isEmpty()) {
                    final NewJson stepsLog = new NewJson();
                    stepsLog.setString("step", currentStep);
                    stepsLog.setList("log", buffer.stream().map(o -> (Object) o).toList());
                    steps.add(stepsLog);
                }
                buffer = new ArrayList<>();
                currentStep = line.replace("---" + id + "---", "");
            } else {
                buffer.add(line);
            }
        }

        buffer = buffer.stream().filter(l -> !l.matches("CICLOPS_EXIT_CODE:.*")).toList();
        if (!buffer.isEmpty()) {
            final NewJson stepsLog = new NewJson();
            stepsLog.setString("step", currentStep);
            stepsLog.setList("log", buffer.stream().map(o -> (Object) o).toList());
            steps.add(stepsLog);
        }

        pipelineLog.setList("steps", steps.stream().map(o -> (Object) o).toList());
    }
}
