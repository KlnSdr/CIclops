package ciclops.credentials;

import java.util.UUID;

public class NullCredentials extends AbstractUsernamePasswordCredentials {
    public NullCredentials(String username, String password, String host, UUID owner) {
        super(username, password, host, "NULL", owner);
    }

    public NullCredentials() {
        super();
    }

    @Override
    public String getFile() {
        return "";
    }
}
