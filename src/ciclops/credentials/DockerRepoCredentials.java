package ciclops.credentials;

import java.util.UUID;

public class DockerRepoCredentials extends AbstractUsernamePasswordCredentials {
    public DockerRepoCredentials(String username, String password, String host, UUID owner) {
        super(username, password, host, "DOCKER", owner);
    }

    public DockerRepoCredentials() {
        super();
    }

    @Override
    public String getFile() {
        return "";
    }
}
