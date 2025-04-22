package ciclops.credentials.rest;

import ciclops.credentials.AbstractUsernamePasswordCredentials;
import ciclops.credentials.NullCredentials;
import ciclops.credentials.service.CredentialsService;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.util.UserUtil;

import java.util.UUID;

public class CredentialsResource {
    private static final CredentialsService service = CredentialsService.getInstance();
    private static final String BASE_PATH = "/rest/credentials";

    @Post(BASE_PATH + "/docker")
    public void addDockerCredentials(HttpContext context) {
        addCredentials(context, "DOCKER");
    }

    @Post(BASE_PATH + "/nyx")
    public void addNyxCredentials(HttpContext context) {
        addCredentials(context, "NYX");
    }

    private void addCredentials(HttpContext context, String type) {
        final NewJson body = context.getRequest().getBody();

        if (!isValidCredentials(body)) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Invalid credentials format");
            context.getResponse().setBody(errorResponse);
            return;
        }

        final String username = body.getString("username");
        final String password = body.getString("password");
        final String host = body.getString("host");

        final NullCredentials credentials = new NullCredentials(username, password, host, UserUtil.getCurrentUserId(context));
        credentials.setType(type); // override type so we can use the same class for all types and don't need to switch on it

        if (!service.saveCredentials(credentials)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Failed to save credentials");
            context.getResponse().setBody(errorResponse);
            return;
        }

        context.getResponse().setCode(ResponseCodes.CREATED);
        final NewJson successResponse = new NewJson();
        successResponse.setString("message", "Credentials saved successfully");
        context.getResponse().setBody(successResponse);
    }

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void getCredentials(HttpContext context) {
        final UUID userId = UserUtil.getCurrentUserId(context);

        final AbstractUsernamePasswordCredentials[] credentials = service.getCredentialsForUser(userId);

        final NewJson json = new NewJson();
        for (final AbstractUsernamePasswordCredentials credential : credentials) {
            json.setString(credential.getKey(), credential.getHost());
        }

        context.getResponse().setBody(json);
    }

    @AuthorizedOnly
    @Delete(BASE_PATH + "/id/{id}")
    public void deleteCredentials(HttpContext context) {
        final String id = context.getRequest().getParam("id");

        if (id == null || id.isEmpty()) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Invalid credentials ID");
            context.getResponse().setBody(errorResponse);
            return;
        }

        if (!service.deleteCredentials(UserUtil.getCurrentUserId(context) + "_" + id)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Failed to delete credentials");
            context.getResponse().setBody(errorResponse);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
        final NewJson successResponse = new NewJson();
        successResponse.setString("message", "Credentials deleted successfully");
        context.getResponse().setBody(successResponse);
    }

    private boolean isValidCredentials(NewJson body) {
        return body.getString("username") != null && body.getString("password") != null && body.getString("host") != null;
    }
}
