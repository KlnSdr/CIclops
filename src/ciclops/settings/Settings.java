package ciclops.settings;

import ciclops.settings.service.SettingsService;
import dobby.util.json.NewJson;
import thot.api.annotations.v2.Bucket;
import thot.janus.DataClass;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

@Bucket(SettingsService.BUCKET_NAME)
public class Settings implements DataClass {
    @JanusUUID("ownerId")
    private UUID ownerId;
    @JanusString("gitUsername")
    private String gitUsername = "";
    @JanusString("gitMail")
    private String gitMail = "";

    public String getGitMail() {
        return gitMail;
    }

    public void setGitMail(String gitMail) {
        this.gitMail = gitMail;
    }

    public String getGitUsername() {
        return gitUsername;
    }

    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getKey() {
        return ownerId.toString();
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("ownerId", ownerId.toString());
        json.setString("gitUsername", gitUsername);
        json.setString("gitMail", gitMail);
        return json;
    }
}
