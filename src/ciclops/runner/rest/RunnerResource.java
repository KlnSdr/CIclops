package ciclops.runner.rest;

import ciclops.runner.RunnerManager;
import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;

import java.util.List;
import java.util.UUID;

public class RunnerResource {
    private static final String BASE_PATH = "/rest/runners";
    private static final RunnerManager manager = RunnerManager.getInstance();

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void getRunningJobs(HttpContext context) {
        final List<UUID> runningJobs = manager.getRunningBuildsList();

        final NewJson json = new NewJson();
        json.setList("runningJobs", runningJobs.stream().map(u -> (Object) u.toString()).toList());

        context.getResponse().setBody(json);
    }
}
