package ciclops.updates;

import ciclops.settings.service.SettingsService;
import hades.update.Update;
import thot.connector.Connector;

public class CreateSettingsBucketUpdate implements Update {
    @Override
    public boolean run() {
        return Connector.write(SettingsService.BUCKET_NAME, "TMP", true) && Connector.delete(SettingsService.BUCKET_NAME, "TMP");
    }

    @Override
    public String getName() {
        return "ciclops_create-settings-bucket";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_SETTINGS_BUCKET.getOrder();
    }
}
