package ciclops.webhooks.rest;

import ciclops.projects.Project;
import ciclops.webhooks.WebhookConfig;
import ciclops.webhooks.service.WebhookConfigService;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.util.UserUtil;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class WebhookResource {
    private static final String BASE_PATH = "/rest/webhooks";
    private static final WebhookConfigService webhookConfigService = WebhookConfigService.getInstance();

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void getAllWebhooksOfUser(HttpContext context) {
        final UUID owner = UserUtil.getCurrentUserId(context);
        final WebhookConfig[] webhooks = webhookConfigService.findByOwner(owner);

        final NewJson payload = new NewJson();

        payload.setList("webhooks", Arrays.stream(webhooks).map(WebhookConfig::toJson).collect(Collectors.toList()));
        context.getResponse().setBody(payload);
    }

    @AuthorizedOnly
    @Post(BASE_PATH)
    public void createWebhook(HttpContext context) {
        final NewJson body = context.getRequest().getBody();

        if (!verifyWebhookDTO(body)) {
            final NewJson error = new NewJson();
            error.setString("error", "Invalid webhook data");
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody(error);
            return;
        }

        final UUID owner = UserUtil.getCurrentUserId(context);
        final String name = body.getString("name");
        final String url = body.getString("url");

        final WebhookConfig webhookConfig = webhookConfigService.create(name, url, owner);

        if (webhookConfig == null) {
            final NewJson error = new NewJson();
            error.setString("error", "Failed to create webhook");
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody(error);
            return;
        }

        context.getResponse().setBody(webhookConfig.toJson());
    }

    @AuthorizedOnly
    @Delete(BASE_PATH + "/id/{id}")
    public void deleteWebhook(HttpContext context) {
        final UUID owner = UserUtil.getCurrentUserId(context);
        final String id = context.getRequest().getParam("id");

        try {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Webhook ID cannot be null or empty");
            }
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            final NewJson error = new NewJson();
            error.setString("error", "Invalid webhook ID format");
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody(error);
            return;
        }

        final WebhookConfig webhookConfig = webhookConfigService.find(owner, UUID.fromString(id));

        if (webhookConfig == null) {
            final NewJson error = new NewJson();
            error.setString("error", "Webhook not found");
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            context.getResponse().setBody(error);
            return;
        }

        if (!webhookConfigService.delete(webhookConfig)) {
            final NewJson error = new NewJson();
            error.setString("error", "Failed to delete webhook");
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody(error);
            return;
        }

        context.getResponse().setCode(ResponseCodes.NO_CONTENT);
    }

    private boolean verifyWebhookDTO(NewJson payload) {
        if (payload == null) {
            return false;
        }
        if (!payload.hasKey("name") || !payload.hasKey("url")) {
            return false;
        }
        if (payload.getString("name") == null || payload.getString("name").isEmpty()) {
            return false;
        }
        return payload.getString("url") != null && !payload.getString("url").isEmpty();
    }
}
