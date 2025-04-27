package ciclops.credentials.service;

import ciclops.credentials.AbstractUsernamePasswordCredentials;
import ciclops.credentials.DockerRepoCredentials;
import ciclops.credentials.NullCredentials;
import ciclops.credentials.NyxCredentials;
import ciclops.security.service.SecurityService;
import dobby.util.json.NewJson;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CredentialsService {
    public static final String BUCKET_NAME = "ciclops_credentials";
    private static CredentialsService instance;
    private static final SecurityService securityService = SecurityService.getInstance();

    private CredentialsService() {
    }

    public static synchronized CredentialsService getInstance() {
        if (instance == null) {
            instance = new CredentialsService();
        }
        return instance;
    }

    public boolean saveCredentials(AbstractUsernamePasswordCredentials credentials) {
        final AbstractUsernamePasswordCredentials encryptedCredentials = encryptCredentials(credentials);
        if (encryptedCredentials == null) {
            return false;
        }
        return Connector.write(BUCKET_NAME, credentials.getKey(), encryptedCredentials.toJson());
    }

    private AbstractUsernamePasswordCredentials encryptCredentials(AbstractUsernamePasswordCredentials credentials) {
        final String encryptedPassword = securityService.encryptForUser(credentials.getPassword(), credentials.getOwner());
        final String encryptedUserName = securityService.encryptForUser(credentials.getUsername(), credentials.getOwner());

        if (encryptedPassword == null || encryptedUserName == null) {
            return null;
        }

        credentials.setPassword(encryptedPassword);
        credentials.setUsername(encryptedUserName);
        return credentials;
    }

    public AbstractUsernamePasswordCredentials getCredentials(String key) {
        final NewJson json = Connector.read(BUCKET_NAME, key, NewJson.class);
        final NullCredentials nullCredentials = Janus.parse(json, NullCredentials.class);

        if (nullCredentials == null) {
            return null;
        }
        return getConcreteImplementation(json);
    }

    public AbstractUsernamePasswordCredentials decryptCredentials(AbstractUsernamePasswordCredentials credentials) {
        final String decryptedPassword = securityService.decryptForUser(credentials.getPassword(), credentials.getOwner());
        final String decryptedUserName = securityService.decryptForUser(credentials.getUsername(), credentials.getOwner());

        if (decryptedPassword == null || decryptedUserName == null) {
            return null;
        }

        credentials.setPassword(decryptedPassword);
        credentials.setUsername(decryptedUserName);
        return credentials;
    }

    public AbstractUsernamePasswordCredentials[] getCredentialsForUser(UUID userId) {
        final NewJson[] jsons = Connector.readPattern(BUCKET_NAME, userId.toString() + "_.*", NewJson.class);
        final List<AbstractUsernamePasswordCredentials> credentials = new ArrayList<>();

        for (final NewJson json : jsons) {
            final NullCredentials nullCredentials = Janus.parse(json, NullCredentials.class);
            if (nullCredentials != null) {
                credentials.add(getConcreteImplementation(json));
            }
        }

        return credentials.toArray(new AbstractUsernamePasswordCredentials[0]);
    }

    public boolean deleteCredentials(String key) {
        return Connector.delete(BUCKET_NAME, key);
    }

    private AbstractUsernamePasswordCredentials getConcreteImplementation(NewJson json) {
        return switch (json.getString("type")) {
            case "DOCKER" -> {
                final DockerRepoCredentials dockerRepoCredentials = new DockerRepoCredentials();
                dockerRepoCredentials.setCredentials(Janus.parse(json, AbstractUsernamePasswordCredentials.class));
                yield dockerRepoCredentials;
            }
            case "NYX" -> {
                final NyxCredentials nyxCredentials = new NyxCredentials();
                nyxCredentials.setCredentials(Janus.parse(json, AbstractUsernamePasswordCredentials.class));
                yield nyxCredentials;
            }
            default -> Janus.parse(json, NullCredentials.class);
        };
    }
}