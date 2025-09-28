package ciclops.settings.rest;

import ciclops.settings.service.BuilderImageService;
import dobby.annotations.Get;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;

public class BuilderImageResource {
    private static final String BASE_PATH = "/rest/builderImage";
    private static final BuilderImageService builderImageService = BuilderImageService.getInstance();

    @Get(BASE_PATH)
    public void getBuilderImage(HttpContext context) {
        final String builderImage = builderImageService.getBuilderImage();
        final NewJson json = new NewJson();
        json.setString("builderImage", builderImage == null ? "" : builderImage);
        context.getResponse().setBody(json);
    }

    @Put(BASE_PATH)
    public void updateBuilderImage(HttpContext context) {
        final NewJson body = context.getRequest().getBody();
        if (body == null || !body.hasKey("builderImage")) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody("Invalid request body");
            return;
        }

        final String newImage = body.getString("builderImage");
        if (newImage == null || newImage.isEmpty()) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody("Builder image cannot be empty");
            return;
        }

        final boolean success = builderImageService.setBuilderImage(newImage);
        if (success) {
            context.getResponse().setBody("Builder image updated successfully");
        } else {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody("Failed to update builder image");
        }
    }
}
