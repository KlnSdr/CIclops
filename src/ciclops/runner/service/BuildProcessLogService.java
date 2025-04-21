package ciclops.runner.service;

import dobby.util.json.NewJson;
import thot.connector.Connector;

import java.util.UUID;

public class BuildProcessLogService {
    public static final String BUCKET_NAME = "ciclops_build_process_log";
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
}
