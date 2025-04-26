package ciclops.projects.rest;

import ciclops.projects.Project;
import ciclops.projects.ProjectBuilder;
import ciclops.projects.UserProjectAssociation;
import ciclops.projects.service.ProjectsService;
import ciclops.runner.RunnerManager;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.util.UserUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectsResource {
    private static final ProjectsService service = ProjectsService.getInstance();
    private static final String BASE_PATH = "/rest/projects";

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void getProjects(HttpContext context) {
        final UserProjectAssociation association = service.getUserProjectAssociation(UserUtil.getCurrentUserId(context));

        final Project[] projects = service.findById(association.getProjects());

        final NewJson json = new NewJson();
        json.setList("projects", Arrays.stream(projects).map(Project::toJson).collect(Collectors.toList()));
        context.getResponse().setBody(json);
    }

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

        final UserProjectAssociation association = service.getUserProjectAssociation(UserUtil.getCurrentUserId(context));

        final Project project = new ProjectBuilder()
                .setProjectName(body.getString("name"))
                .setGitUrl(body.getString("gitUrl"))
                .setOwner(UserUtil.getCurrentUserId(context))
                .build();

        association.addProject(project.getId());

        if (!service.update(project)) {
            final NewJson error = new NewJson();
            error.setString("error", "Failed to create project");
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody(error);
            return;
        }

        if (!service.updateUserProjectAssociation(association)) {
            final NewJson error = new NewJson();
            error.setString("error", "Failed to update user project association");
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody(error);
            return;
        }

        context.getResponse().setCode(ResponseCodes.CREATED);
        context.getResponse().setBody(project.toJson());
        context.getResponse().setHeader("Location", BASE_PATH + "/id/" + project.getId());
    }

    @Post(BASE_PATH + "/id/{id}/webhook/github")
    public void webhookGithub(HttpContext context) {
        final NewJson body = context.getRequest().getBody();
        if (body == null) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final String id = context.getRequest().getParam("id");
        final Project project = service.findById(id);

        if (project == null) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return;
        }
        new GithubPushWebhookHandler(project, body).handle();
        context.getResponse().setCode(ResponseCodes.ACCEPTED);
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

    @AuthorizedOnly
    @Post(BASE_PATH + "/id/{id}/trigger-build")
    public void triggerBuild(HttpContext context) {
        final String id = context.getRequest().getParam("id");
        final Project project = service.findById(id);

        if (project == null || !project.getOwner().equals(UserUtil.getCurrentUserId(context))) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return;
        }

        RunnerManager.getInstance().addBuildToQueue(project.getId());
    }

    @AuthorizedOnly
    @Post(BASE_PATH + "/id/{id}/trigger-build/release")
    public void triggerReleaseBuild(HttpContext context) {
        final String id = context.getRequest().getParam("id");
        final Project project = service.findById(id);

        if (project == null || !project.getOwner().equals(UserUtil.getCurrentUserId(context))) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return;
        }

        RunnerManager.getInstance().addReleaseBuildToQueue(project.getId());
    }

    @AuthorizedOnly
    @Put(BASE_PATH + "/id/{id}/credentials")
    public void updateCredentials(HttpContext context) {
        final String id = context.getRequest().getParam("id");
        final NewJson body = context.getRequest().getBody();

        if (!body.hasKey("credentials") || body.getList("credentials") == null) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final List<Object> credentials = body.getList("credentials");
        if (!credentials.stream().allMatch(o -> o instanceof String)) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final Project project = service.findById(id);
        if (project == null || !project.getOwner().equals(UserUtil.getCurrentUserId(context))) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return;
        }

        project.setCredentials(credentials.stream().map(o -> (String) o).collect(Collectors.toList()));

        if (!service.update(project)) {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            return;
        }
        context.getResponse().setCode(ResponseCodes.OK);
    }

    private boolean verifyProjectDTO(NewJson body) {
        if (body == null) {
            return false;
        }
        if (!body.hasKeys("name", "gitUrl")) {
            return false;
        }

        return body.getString("name") != null
                && !body.getString("name").isEmpty()
                && body.getString("gitUrl") != null
                && !body.getString("gitUrl").isEmpty();
    }
}
