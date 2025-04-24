package ciclops.credentials.fileGenerators;

import ciclops.credentials.DockerRepoCredentials;
import dobby.util.json.NewJson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class DockerLoginFileGenerator implements FileGenerator {
    private final List<DockerRepoCredentials> credentialsList;

    public DockerLoginFileGenerator(List<DockerRepoCredentials> credentialsList) {
        this.credentialsList = credentialsList;
    }

    @Override
    public String getFileName() {
        return "docker.json";
    }

    @Override
    public String getContainerFilePath() {
        return "/root/.config/containers/auth.json";
    }

    @Override
    public String getFileContent() {
        final NewJson json = new NewJson();
        final NewJson auths = new NewJson();

        for (DockerRepoCredentials credentials : credentialsList) {
            String repoUrl = credentials.getHost();
            String username = credentials.getUsername();
            String password = credentials.getPassword();

            if (repoUrl == null || username == null || password == null) {
                continue;
            }

            final NewJson registry = new NewJson();
            registry.setString("auth", Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
            auths.setJson(repoUrl.replace(".", "|"), registry);
        }


        json.setJson("auths", auths);

        return json.toString().replace("|", ".");
    }
}
