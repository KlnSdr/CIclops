package ciclops.runner.service;

import ciclops.runner.LastRuns;
import dobby.util.json.NewJson;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.UUID;

public class BuildProcessLogService {
    public static final String BUCKET_NAME = "ciclops_build_process_log";
    public static final String LAST_RUNS_BUCKET_NAME = "ciclops_last_runs";
    private static BuildProcessLogService instance;

    private BuildProcessLogService() {
        // Private constructor to prevent instantiation
    }

    public static synchronized BuildProcessLogService getInstance() {
        if (instance == null) {
            instance = new BuildProcessLogService();
        }
        return instance;
    }

    public boolean saveLog(UUID id, UUID projectId, NewJson log) {
        return Connector.write(BUCKET_NAME, id + "_" + projectId, log);
    }

    public NewJson getLog(String key) {
        return Connector.read(BUCKET_NAME, key, NewJson.class);
    }

    public boolean addLogLastRuns(UUID projectId, NewJson log) {
        final LastRuns lastRuns = getLastRuns(projectId);
        lastRuns.addRun(log.getString("id") + "_" + projectId);
        return Connector.write(LAST_RUNS_BUCKET_NAME, lastRuns.getKey(), lastRuns.toJson());
    }

    public LastRuns getLastRuns(UUID projectId) {
        final LastRuns lastRuns = Janus.parse(Connector.read(LAST_RUNS_BUCKET_NAME, projectId.toString(), NewJson.class), LastRuns.class);

        if (lastRuns == null) {
            return new LastRuns(projectId);
        }
        return lastRuns;
    }
}
