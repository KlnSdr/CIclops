package ciclops.projects;

import java.util.UUID;

public class ProjectBuilder {
    private String projectName;
    private String gitUrl;
    private UUID owner;

    public ProjectBuilder setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ProjectBuilder setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
        return this;
    }

    public ProjectBuilder setOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

    public Project build() {
        final Project project = new Project();
        project.setName(projectName);
        project.setGitUrl(gitUrl);
        project.setOwner(owner);
        return project;
    }

    public Project buildWithId(UUID id) {
        final Project project = build();
        project.setId(id);
        return project;
    }
}
