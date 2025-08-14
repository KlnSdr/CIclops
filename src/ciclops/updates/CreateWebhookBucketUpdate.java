package ciclops.updates;

import ciclops.webhooks.service.WebhookConfigService;
import hades.update.Update;
import thot.connector.Connector;

public class CreateWebhookBucketUpdate implements Update {
    @Override
    public boolean run() {
        return Connector.write(WebhookConfigService.BUCKET_NAME, "TMP", true) && Connector.delete(WebhookConfigService.BUCKET_NAME, "TMP");
    }

    @Override
    public String getName() {
        return "ciclops_create-webhook-bucket";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_WEBHOOK_BUCKET.getOrder();
    }
}
