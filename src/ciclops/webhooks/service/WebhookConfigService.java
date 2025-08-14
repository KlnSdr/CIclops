package ciclops.webhooks.service;

import ciclops.webhooks.WebhookConfig;
import dobby.util.json.NewJson;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.UUID;

public class WebhookConfigService {
    public static final String BUCKET_NAME = "ciclops_webhooks";
    private static WebhookConfigService instance;

    private WebhookConfigService() {
    }

    public static WebhookConfigService getInstance() {
        if (instance == null) {
            instance = new WebhookConfigService();
        }
        return instance;
    }

    public WebhookConfig create(String name, String url, UUID owner) {
        final WebhookConfig config = new WebhookConfig();
        config.setName(name);
        config.setUrl(url);
        config.setOwner(owner);

        final boolean success = Connector.write(BUCKET_NAME, config.getKey(), config.toJson());

        if (!success) {
            return null;
        }

        return config;
    }

    public WebhookConfig find(UUID owner, UUID id) {
        final String key = owner + "_" + id;
        return Janus.parse(Connector.read(BUCKET_NAME, key, NewJson.class), WebhookConfig.class);
    }

    public WebhookConfig[] findByOwner(UUID owner) {
        final NewJson[] jsons = Connector.readPattern(BUCKET_NAME, owner.toString() + "_.*", NewJson.class);
        final WebhookConfig[] configs = new WebhookConfig[jsons.length];
        for (int i = 0; i < jsons.length; i++) {
            configs[i] = Janus.parse(jsons[i], WebhookConfig.class);
        }
        return configs;
    }

    public boolean delete(WebhookConfig config) {
        return Connector.delete(BUCKET_NAME, config.getKey());
    }
}
