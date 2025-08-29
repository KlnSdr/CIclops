package ciclops.runner;

import common.logger.Logger;
import dobby.Config;
import dobby.task.SchedulerService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class RunnerManager {
    private static RunnerManager instance;
    private final Logger LOGGER = new Logger(RunnerManager.class);
    private final ConcurrentLinkedQueue<UUID> buildQueue;
    private final ConcurrentLinkedQueue<UUID> releaseBuildQueue;
    private final List<UUID> runningBuildsList;
    private int runningBuilds = 0;

    private RunnerManager() {
        this.buildQueue = new ConcurrentLinkedQueue<>();
        this.releaseBuildQueue = new ConcurrentLinkedQueue<>();
        this.runningBuildsList = new ArrayList<>();

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

    public synchronized void addReleaseBuildToQueue(UUID projectId) {
        LOGGER.debug("Adding build to queue: " + projectId);
        releaseBuildQueue.add(projectId);
    }

    private synchronized void checkQueue() {
        LOGGER.debug("Checking build queue");
        final int maxRunningBuilds = Config.getInstance().getInt("application.runner.maxCount", 5);
        LOGGER.debug("running builds: " + getRunningBuilds());
        if (getRunningBuilds() >= maxRunningBuilds) {
            return;
        }
        while (getRunningBuilds() < maxRunningBuilds && !releaseBuildQueue.isEmpty()) {
            final UUID projectId = releaseBuildQueue.poll();
            if (projectId == null) {
                continue;
            }
            LOGGER.debug("Starting release build for project: " + projectId);
            spawnRunnerThread(projectId, true);
        }

        while (getRunningBuilds() < maxRunningBuilds && !buildQueue.isEmpty()) {
            final UUID projectId = buildQueue.poll();
            if (projectId == null) {
                continue;
            }
            LOGGER.debug("Starting build for project: " + projectId);
            spawnRunnerThread(projectId, false);
        }
    }

    private void spawnRunnerThread(UUID projectId, boolean isRelease) {
        final int timeout = Config.getInstance().getInt("application.runner.timeout", 60);
        incrementRunningBuilds();
        final UUID runnerId = UUID.randomUUID();
        addRunningBuild(runnerId);
        final Runner runner = new Runner(runnerId, projectId, isRelease);
        final Thread timeoutChecker = new Thread(() -> {
            final Thread worker = new Thread(() -> {
                try {
                    runner.start();
                } catch (Exception e) {
                    LOGGER.error("Error starting build for project: " + projectId);
                    LOGGER.trace(e);
                }
            });
            worker.start();
            try {
                worker.join(timeout * 60000L);
                if (worker.isAlive()) {
                    LOGGER.warn("Timed out waiting for build to finish for project: " + projectId);
                    runner.abort();
                    worker.interrupt();
                }
            } catch(InterruptedException ex) {
                LOGGER.warn("Interrupted while waiting for build to finish for project: " + projectId);
                runner.abort();
                worker.interrupt();
            } finally {
                decrementRunningBuilds();
                removeRunningBuild(runnerId);
            }
        });
        timeoutChecker.start();
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

    public List<UUID> getRunningBuildsList() {
        return List.copyOf(runningBuildsList);
    }

    private synchronized void addRunningBuild(UUID buildId) {
        runningBuildsList.add(buildId);
    }

    private synchronized void removeRunningBuild(UUID buildId) {
        runningBuildsList.remove(buildId);
    }
}
