package ciclops.credentials.fileGenerators;

import ciclops.credentials.NyxCredentials;
import dobby.util.json.NewJson;

import java.util.List;

public class NyxLoginFileGenerator implements FileGenerator {
    private final List<NyxCredentials> credentialsList;

    public NyxLoginFileGenerator(List<NyxCredentials> credentialsList) {
        this.credentialsList = credentialsList;
    }

    @Override
    public String getFileName() {
        return "nyx.json";
    }

    @Override
    public String getContainerFilePath() {
        return "/root/.nyx/settings.json";
    }

    @Override
    public String getFileContent() {
        final NewJson json = new NewJson();

        json.setList("repos", credentialsList.stream().map(credentials -> {
            final NewJson credentialJson = new NewJson();
            credentialJson.setString("repo", credentials.getHost());
            credentialJson.setString("token", credentials.getPassword());
            return (Object) credentialJson;
        }).toList());

        return json.toString();
    }
}
