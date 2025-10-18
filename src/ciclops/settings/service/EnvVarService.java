package ciclops.settings.service;

import ciclops.settings.EnvVar;
import dobby.util.json.NewJson;
import hades.security.service.SecurityService;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnvVarService {
    public static final String BUCKET_NAME = "ciclops_env_vars";
    private static EnvVarService instance;

    private static final SecurityService securityService = SecurityService.getInstance();

    private EnvVarService() {
    }

    public static synchronized EnvVarService getInstance() {
        if (instance == null) {
            instance = new EnvVarService();
        }
        return instance;
    }

    public List<EnvVar> getEnvVarsForUser(UUID userId) {
        final NewJson[] jsons = Connector.readPattern(BUCKET_NAME, userId + "_.*", NewJson.class);
        final List<EnvVar> envVars = new ArrayList<>();

        for (NewJson json : jsons) {
            final EnvVar envVar = Janus.parse(json, EnvVar.class);
            if (envVar != null) {
                final EnvVar decryptedEnvVar = decryptEnvVar(envVar);
                if (decryptedEnvVar != null) {
                    envVars.add(decryptedEnvVar);
                }
            }
        }
        return envVars;
    }

    public boolean saveEnvVar(EnvVar envVar) {
        final EnvVar encryptedEnvVar = encryptEnvVar(envVar);
        if (encryptedEnvVar == null) {
            return false;
        }
        return Connector.write(BUCKET_NAME, envVar.getKey(), encryptedEnvVar.toJson());
    }

    private EnvVar encryptEnvVar(EnvVar envVar) {
        final String encryptedValue = securityService.encryptForUser(envVar.getValue(), envVar.getOwnerId());
        if (encryptedValue == null) {
            return null;
        }
        envVar.setValue(encryptedValue);
        return envVar;
    }

    private EnvVar decryptEnvVar(EnvVar envVar) {
        final String decryptedValue = securityService.decryptForUser(envVar.getValue(), envVar.getOwnerId());
        if (decryptedValue == null) {
            return null;
        }
        envVar.setValue(decryptedValue);
        return envVar;
    }
}
