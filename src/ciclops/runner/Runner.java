package ciclops.runner;

import ciclops.projects.Project;
import ciclops.projects.service.ProjectsService;
import common.logger.Logger;
import dobby.util.json.NewJson;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Comparator;
import java.util.UUID;

public class Runner {
    private final Logger LOGGER = new Logger(Runner.class);
    private final UUID id;
    private final UUID projectId;

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
            Files.walk(tmpDirFile.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
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

        final String command = "podman run --privileged -v /tmp/ciclops/" + id + "/auth.json:/root/.config/containers/auth.json:ro --rm ciclopsbuilder:0.16";
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.error("Failed to execute command \"" + command + "\". Exit code: " + exitCode);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing command: " + command);
            LOGGER.trace(e);
        }
    }
}
