package ciclops.settings.service;

import thot.connector.Connector;

public class BuilderImageService {
    private static BuilderImageService instance;
    private String builderImage;

    private BuilderImageService() {
    }

    public static BuilderImageService getInstance() {
        if (instance == null) {
            instance = new BuilderImageService();
        }
        return instance;
    }

    private void readBuilderImage() {
        builderImage = Connector.read("system", "ciclops_builder_image", String.class);
    }

    public String getBuilderImage() {
        if (builderImage == null) {
            readBuilderImage();
        }

        return builderImage;
    }

    public boolean setBuilderImage(String image) {
        final boolean success = Connector.write("system", "ciclops_builder_image", image);
        if (success) {
            builderImage = image;
        }
        return success;
    }
}
