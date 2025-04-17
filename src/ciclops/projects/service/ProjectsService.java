package ciclops.projects.service;

import ciclops.projects.Project;
import dobby.util.json.NewJson;
import thot.connector.Connector;
import thot.janus.Janus;

public class ProjectsService {
    public static final String BUCKET_NAME = "ciclops_projects";
    private static ProjectsService instance;

    private ProjectsService() {
    }

    public static ProjectsService getInstance() {
        if (instance == null) {
            instance = new ProjectsService();
        }
        return instance;
    }

    public boolean update(Project project) {
        return Connector.write(BUCKET_NAME, project.getKey(), project.toJson());
    }

    public Project findById(String id) {
        return Janus.parse(Connector.read(BUCKET_NAME, id, NewJson.class), Project.class);
    }
}
