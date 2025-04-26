package ciclops.projects.rest;

import ciclops.projects.Project;
import ciclops.runner.RunnerManager;
import common.logger.Logger;
import dobby.util.json.NewJson;

public class GithubPushWebhookHandler {
    private static final Logger LOGGER = new Logger(GithubPushWebhookHandler.class);
    private final Project project;
    private final NewJson hookData;

    public GithubPushWebhookHandler(Project project, NewJson hookData) {
        this.project = project;
        this.hookData = hookData;
    }

    public void handle() {
        // TODO check headers for webhook type, assume push for now

        LOGGER.debug("adding build for project " + project.getName() + "(" + project.getId() + ") to queue");
        RunnerManager.getInstance().addBuildToQueue(project.getId());
    }
}
