package ciclops.credentials;

import dobby.util.json.NewJson;
import thot.janus.DataClass;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

public class AbstractUsernamePasswordCredentials implements DataClass {
    @JanusString("username")
    private String username;
    @JanusString("password")
    private String password;
    @JanusString("host")
    private String host;
    @JanusString("type")
    private String type;
    @JanusUUID("owner")
    private UUID owner;
    @JanusUUID("id")
    private UUID id;

    private AbstractUsernamePasswordCredentials credentials;

    public AbstractUsernamePasswordCredentials(String username, String password, String host, String type, UUID owner) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.host = host;
        this.type = type;
        this.owner = owner;
    }

    public AbstractUsernamePasswordCredentials() {
        this.id = UUID.randomUUID();
    }

    public String getUsername() {
        if (credentials != null) {
            return credentials.getUsername();
        }
        return username;
    }

    public void setUsername(String username) {
        if (credentials != null) {
            credentials.setUsername(username);
        }
        this.username = username;
    }

    public String getPassword() {
        if (credentials != null) {
            return credentials.getPassword();
        }
        return password;
    }

    public void setPassword(String password) {
        if (credentials != null) {
            credentials.setPassword(password);
        }
        this.password = password;
    }

    public String getHost() {
        if (credentials != null) {
            return credentials.getHost();
        }
        return host;
    }

    public void setHost(String host) {
        if (credentials != null) {
            credentials.setHost(host);
        }
        this.host = host;
    }

    public String getType() {
        if (credentials != null) {
            return credentials.getType();
        }
        return type;
    }

    public void setType(String type) {
        if (credentials != null) {
            credentials.setType(type);
        }
        this.type = type;
    }

    public UUID getOwner() {
        if (credentials != null) {
            return credentials.getOwner();
        }
        return owner;
    }

    public void setOwner(UUID owner) {
        if (credentials != null) {
            credentials.setOwner(owner);
        }
        this.owner = owner;
    }

    public UUID getId() {
        if (credentials != null) {
            return credentials.getId();
        }
        return id;
    }

    public void setCredentials(AbstractUsernamePasswordCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String getKey() {
        if (credentials != null) {
            return credentials.getKey();
        }
        return owner + "_" + id;
    }

    @Override
    public NewJson toJson() {
        if (credentials != null) {
            return credentials.toJson();
        }
        final NewJson json = new NewJson();
        json.setString("username", username);
        json.setString("password", password);
        json.setString("host", host);
        json.setString("type", type);
        json.setString("owner", owner.toString());
        json.setString("id", id.toString());
        return json;
    }
}
