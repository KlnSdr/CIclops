package ciclops.credentials;

import java.util.UUID;

public class NyxCredentials extends AbstractUsernamePasswordCredentials {
    public NyxCredentials(String username, String password, String host, UUID owner) {
        super(username, password, host, "NYX", owner);
    }

    public NyxCredentials() {
        super();
    }

    @Override
    public String getFile() {
        return "";
    }
}
