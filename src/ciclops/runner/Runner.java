package ciclops.runner;

import ciclops.projects.Project;
import ciclops.projects.service.ProjectsService;
import ciclops.runner.service.BuildProcessLogService;
import common.logger.Logger;
import dobby.util.json.NewJson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Runner {
    private final Logger LOGGER = new Logger(Runner.class);
    private final UUID id;
    private final UUID projectId;
    private static final String BUILD_POD_IMAGE = "ciclopsbuilder:0.22";

    public Runner(UUID id, UUID projectId) {
        this.id = id;
        this.projectId = projectId;
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
        final String loginFileJson = generateLoginFile("USERNAME", "PASSWORD", "REPO");

        if (!writeAuthFile(loginFileJson)) {
            cleanup();
            LOGGER.error("Failed to write auth file for project " + project.getName());
            return;
        }
        initBuildPod(scmUrl);
        cleanup();
    }

    private boolean writeAuthFile(String content) {
        final String authFilePath = "/tmp/ciclops/" + id + "/auth.json";
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

    private String generateLoginFile(String username, String password, String repoUrl) {
        final NewJson registry = new NewJson();
        registry.setString("auth", Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
        final NewJson auths = new NewJson();
        auths.setJson("REPO", registry);
        final NewJson json = new NewJson();
        json.setJson("auths", auths);

        return json.toString().replace("REPO", repoUrl);
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

    private void initBuildPod(String scmUrl) {
        // TODO
        // set env variables
        // - scmURL
        // - inject git credentials
        final String separator = "---" + id + "---";

        final String command = "podman run -e SEPARATOR=" + separator + " --privileged -v /tmp/ciclops/" + id + "/auth.json:/root/.config/containers/auth.json:ro --rm " + BUILD_POD_IMAGE;
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
        splitIntoSteps(processLog, output);

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

        String currentStep = "pod init";
        List<String> buffer = new ArrayList<>();

        for (String line : processedLogs) {
            if (line.contains("---" + id + "---")) {
                if (!buffer.isEmpty()) {
                    final NewJson stepsLog = new NewJson();
                    stepsLog.setString("step", currentStep);
                    stepsLog.setList("log", buffer.stream().map(o -> (Object) o).toList());
                    steps.add(stepsLog);
                    buffer = new ArrayList<>();
                }
                currentStep = line.replace("---" + id + "---", "");
            } else {
                buffer.add(line);
            }
        }

        if (!buffer.isEmpty()) {
            final NewJson stepsLog = new NewJson();
            stepsLog.setString("step", currentStep);
            stepsLog.setList("log", buffer.stream().map(o -> (Object) o).toList());
            steps.add(stepsLog);
        }

        pipelineLog.setList("steps", steps.stream().map(o -> (Object) o).toList());
    }
}
