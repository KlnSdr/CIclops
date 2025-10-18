package ciclops.settings.rest;

import ciclops.settings.EnvVar;
import ciclops.settings.service.EnvVarService;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.util.UserUtil;

import java.util.List;

public class EnvVarResource {
    private final EnvVarService envVarService = EnvVarService.getInstance();
    private static final String BASE_PATH = "/rest/env-vars";

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void getEnvVars(HttpContext context) {
        final NewJson json = new NewJson();
        final List<EnvVar> envVars = envVarService.getEnvVarsForUser(UserUtil.getCurrentUserId(context));
        final List<String> envVarNames = envVars.stream().map(EnvVar::getName).toList();

        json.setList("envVars", envVarNames.stream().map(u -> (Object) u).toList());
        context.getResponse().setBody(json);
    }

    @AuthorizedOnly
    @Post(BASE_PATH)
    public void createEnvVar(HttpContext context) {
        final NewJson body = context.getRequest().getBody();

        if (!body.hasKeys("name", "value")) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Missing 'name' or 'value' in request body");
            context.getResponse().setBody(errorResponse);
            return;
        }

        final String name = body.getString("name");
        final String value = body.getString("value");

        if (name == null || name.isEmpty() || value == null) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "'name' must be non-empty and 'value' must be non-null");
            context.getResponse().setBody(errorResponse);
            return;
        }

        final EnvVar envVar = new EnvVar();
        envVar.setName(name);
        envVar.setValue(value);
        envVar.setOwnerId(UserUtil.getCurrentUserId(context));
        if (!envVarService.saveEnvVar(envVar)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Failed to save environment variable");
            context.getResponse().setBody(errorResponse);
            return;
        }
        context.getResponse().setCode(ResponseCodes.CREATED);
    }
}
