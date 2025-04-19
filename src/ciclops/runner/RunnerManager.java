package ciclops.runner;

import common.logger.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RunnerManager {
    private static RunnerManager instance;
    private final Logger LOGGER = new Logger(RunnerManager.class);
    private final ConcurrentLinkedQueue<UUID> buildQueue;
    private int runningBuilds = 0;

    private RunnerManager() {
        this.buildQueue = new ConcurrentLinkedQueue<>();
    }

    public static synchronized RunnerManager getInstance() {
        if (instance == null) {
            instance = new RunnerManager();
        }
        return instance;
    }

    public synchronized void addBuildToQueue(UUID projectId) {
        LOGGER.debug("Adding build to queue: " + projectId);
        buildQueue.add(projectId);
    }
}
