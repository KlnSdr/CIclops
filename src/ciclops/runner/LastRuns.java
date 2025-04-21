package ciclops.runner;

import dobby.util.json.NewJson;
import thot.janus.DataClass;
import thot.janus.annotations.JanusList;
import thot.janus.annotations.JanusUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LastRuns implements DataClass {
    @JanusUUID("projectId")
    private UUID projectId;
    @JanusList("runs")
    private List<String> runs = new ArrayList<>();

    public LastRuns() {
    }

    public LastRuns(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public List<String> getRuns() {
        return runs;
    }

    public void addRun(String runId) {
        if (runs.size() >= 5) {
            runs.removeFirst();
        }
        runs.add(runId);
    }

    public void removeRun(String runId) {
        runs.remove(runId);
    }

    @Override
    public String getKey() {
        return projectId.toString();
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("projectId", projectId.toString());
        json.setList("runs", runs.stream().map(o -> (Object) o).toList());
        return json;
    }
}
