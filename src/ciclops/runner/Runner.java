package ciclops.runner;

import common.logger.Logger;

import java.util.UUID;

public class Runner {
    private final Logger LOGGER = new Logger(Runner.class);
    private final UUID id;

    public Runner(UUID id) {
        this.id = id;
    }

    public void start() {
        LOGGER.debug("Starting runner " + id);
    }
}
