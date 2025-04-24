package ciclops.credentials.fileGenerators;

public interface FileGenerator {
    String getFileName();
    String getContainerFilePath();
    String getFileContent();
}
