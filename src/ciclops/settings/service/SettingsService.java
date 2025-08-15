package ciclops.settings.service;

import ciclops.settings.Settings;
import dobby.util.json.NewJson;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.UUID;

public class SettingsService {
    public static final String BUCKET_NAME = "ciclops_settings";
    private static SettingsService instance;

    private SettingsService() {
    }

    public static SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public boolean save(Settings settings) {
        return Connector.write(BUCKET_NAME, settings.getKey(), settings.toJson());
    }

    public Settings find(UUID ownerId) {
        return Janus.parse(Connector.read(BUCKET_NAME, ownerId.toString(), NewJson.class), Settings.class);
    }
}
