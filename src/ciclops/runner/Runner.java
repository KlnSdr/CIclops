package ciclops.runner;

import ciclops.projects.Project;
import ciclops.projects.service.ProjectsService;
import common.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        initBuildPod(scmUrl);
    }

    private void initBuildPod(String scmUrl) {
        // TODO
        // set env variables
        // - scmURL
        // - inject git credentials

        final String command = "podman run --rm ciclopsbuilder:0.6";
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            final Process process = processBuilder.start();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug(line);
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
