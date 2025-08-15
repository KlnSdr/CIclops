package ciclops.settings.rest;

import ciclops.settings.Settings;
import ciclops.settings.service.SettingsService;
import dobby.annotations.Get;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.util.UserUtil;

import java.util.UUID;

public class SettingsResource {
    private static final String BASE_PATH = "/rest/settings";
    private static final SettingsService service = SettingsService.getInstance();

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void getSettings(HttpContext context) {
        final UUID userId = UserUtil.getCurrentUserId(context);

        if (userId == null) {
            context.getResponse().setCode(ResponseCodes.UNAUTHORIZED);
            context.getResponse().setBody("User ID is required");
            return;
        }

        Settings settings = service.find(userId);

        if (settings == null) {
            settings = new Settings();
            settings.setOwnerId(userId);
        }

        context.getResponse().setBody(settings.toJson());
    }

    @AuthorizedOnly
    @Put(BASE_PATH)
    public void updateSettings(HttpContext context) {
        final UUID userId = UserUtil.getCurrentUserId(context);

        if (userId == null) {
            context.getResponse().setCode(ResponseCodes.UNAUTHORIZED);
            context.getResponse().setBody("User ID is required");
            return;
        }

        final NewJson body = context.getRequest().getBody();

        if (!verifySettingsDTO(body)) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody("Invalid settings data");
            return;
        }

        final Settings settings = new Settings();
        settings.setOwnerId(userId);
        settings.setGitUsername(body.getString("gitUsername"));
        settings.setGitMail(body.getString("gitMail"));

        if (!service.save(settings)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody("Failed to save settings");
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
        context.getResponse().setBody("Settings updated successfully");
    }

    private boolean verifySettingsDTO(NewJson body) {
        return body != null && body.hasKey("gitUsername") && body.hasKey("gitMail");
    }
}
