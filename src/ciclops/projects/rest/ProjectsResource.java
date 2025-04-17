package ciclops.projects.rest;

import ciclops.projects.Project;
import ciclops.projects.ProjectBuilder;
import ciclops.projects.service.ProjectsService;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.util.UserUtil;

public class ProjectsResource {
    private static final ProjectsService service = ProjectsService.getInstance();
    private static final String BASE_PATH = "/rest/projects";

    @AuthorizedOnly
    @Post(BASE_PATH)
    public void createProject(HttpContext context) {
        final NewJson body = context.getRequest().getBody();

        if (!verifyProjectDTO(body)) {
            final NewJson error = new NewJson();
            error.setString("error", "Invalid project data");
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody(error);
            return;
        }

        final Project project = new ProjectBuilder()
                .setProjectName(body.getString("name"))
                .setGitUrl(body.getString("gitUrl"))
                .setOwner(UserUtil.getCurrentUserId(context))
                .build();

        if (!service.update(project)) {
            final NewJson error = new NewJson();
            error.setString("error", "Failed to create project");
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody(error);
            return;
        }

        context.getResponse().setCode(ResponseCodes.CREATED);
        context.getResponse().setBody(project.toJson());
        context.getResponse().setHeader("Location", BASE_PATH + "/id/" + project.getId());
    }

    @AuthorizedOnly
    @Get(BASE_PATH + "/id/{id}")
    public void getProjectById(HttpContext context) {
        final String id = context.getRequest().getParam("id");
        final Project project = service.findById(id);

        if (project == null || !project.getOwner().equals(UserUtil.getCurrentUserId(context))) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return;
        }

        context.getResponse().setCode(ResponseCodes.OK);
        context.getResponse().setBody(project.toJson());
    }

    private boolean verifyProjectDTO(NewJson body) {
        if (body == null) {
            return false;
        }
        if (!body.hasKeys("name", "gitUrl")) {
            return false;
        }

        return body.getString("name") != null && !body.getString("name").isEmpty();
    }
}
