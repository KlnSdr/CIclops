package ciclops.settings;

import dobby.util.json.NewJson;
import thot.api.annotations.v2.Bucket;
import thot.janus.DataClass;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

import static ciclops.settings.service.EnvVarService.BUCKET_NAME;

@Bucket(BUCKET_NAME)
public class EnvVar implements DataClass {
    @JanusUUID("ownerId")
    private UUID ownerId;
    @JanusString("name")
    private String name;
    @JanusString("value")
    private String value;

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getKey() {
        return ownerId + "_" + name;
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("ownerId", ownerId.toString());
        json.setString("name", name);
        json.setString("value", value);
        return json;
    }
}
