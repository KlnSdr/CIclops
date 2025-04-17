package ciclops.projects.service;

import ciclops.projects.Project;
import ciclops.projects.UserProjectAssociation;
import dobby.util.json.NewJson;
import thot.connector.Connector;
import thot.janus.Janus;

import java.util.List;
import java.util.UUID;

public class ProjectsService {
    public static final String BUCKET_NAME = "ciclops_projects";
    public static final String USER_PROJECT_ASSOCIATION_BUCKET_NAME = "ciclops_user_project_association";
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

    public Project[] findById(List<UUID> ids) {
        return ids.stream()
                .map(id -> Janus.parse(Connector.read(BUCKET_NAME, id.toString(), NewJson.class), Project.class))
                .toArray(Project[]::new);
    }

    public UserProjectAssociation getUserProjectAssociation(UUID userId) {
        final UserProjectAssociation assoc = Janus.parse(Connector.read(USER_PROJECT_ASSOCIATION_BUCKET_NAME, userId.toString(), NewJson.class), UserProjectAssociation.class);
        if (assoc == null) {
            return new UserProjectAssociation(userId);
        }
        return assoc;
    }

    public boolean updateUserProjectAssociation(UserProjectAssociation userProjectAssociation) {
        return Connector.write(USER_PROJECT_ASSOCIATION_BUCKET_NAME, userProjectAssociation.getKey(), userProjectAssociation.toJson());
    }
}
