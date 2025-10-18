package ciclops.updates;

import ciclops.settings.service.EnvVarService;
import hades.update.Update;
import thot.connector.Connector;

public class CreateEnvVarBucketUpdate implements Update {
    @Override
    public boolean run() {
        return Connector.write(EnvVarService.BUCKET_NAME, "TMP", true) && Connector.delete(EnvVarService.BUCKET_NAME, "TMP");
    }

    @Override
    public String getName() {
        return "ciclops_create-env-var-bucket";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_ENV_VAR_BUCKET.getOrder();
    }
}
