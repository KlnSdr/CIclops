package ciclops.runner;

import common.logger.Logger;

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
        try {
            Thread.sleep(10000 + (long) (Math.random() * 10000)); // Simulate build time
        } catch (InterruptedException e) {
            LOGGER.error("Runner interrupted: " + id);
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.debug("Runner " + id + " finished");
        }
    }
}
