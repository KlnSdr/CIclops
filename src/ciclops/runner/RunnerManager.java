package ciclops.runner;

import common.logger.Logger;
import dobby.Config;
import dobby.task.SchedulerService;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class RunnerManager {
    private static RunnerManager instance;
    private final Logger LOGGER = new Logger(RunnerManager.class);
    private final ConcurrentLinkedQueue<UUID> buildQueue;
    private int runningBuilds = 0;

    private RunnerManager() {
        this.buildQueue = new ConcurrentLinkedQueue<>();

        LOGGER.info("adding build queue check task");
        SchedulerService.getInstance().addRepeating(this::checkQueue, Config.getInstance().getInt("application.runner.checkCount", 5), TimeUnit.SECONDS);
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

    private synchronized void checkQueue() {
        LOGGER.debug("Checking build queue");
        final int maxRunningBuilds = Config.getInstance().getInt("application.runner.maxCount", 5);
        LOGGER.debug("running builds: " + getRunningBuilds());
        if (getRunningBuilds() >= maxRunningBuilds) {
            return;
        }
        while (getRunningBuilds() < maxRunningBuilds && !buildQueue.isEmpty()) {
            final UUID projectId = buildQueue.poll();
            if (projectId == null) {
                continue;
            }
            LOGGER.debug("Starting build for project: " + projectId);
            incrementRunningBuilds();
            new Thread(() -> {
                try {
                    final UUID runnerId = UUID.randomUUID();
                    final Runner runner = new Runner(runnerId, projectId);
                    runner.start();
                } catch (Exception e) {
                    LOGGER.error("Error starting build for project: " + projectId);
                    LOGGER.trace(e);
                } finally {
                    decrementRunningBuilds();
                }
            }).start();
        }
    }

    private synchronized void incrementRunningBuilds() {
        runningBuilds++;
    }

    private synchronized void decrementRunningBuilds() {
        runningBuilds--;
    }

    private synchronized int getRunningBuilds() {
        return runningBuilds;
    }
}
