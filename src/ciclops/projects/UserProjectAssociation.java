package ciclops.projects;

import dobby.util.json.NewJson;
import thot.janus.DataClass;
import thot.janus.annotations.JanusList;
import thot.janus.annotations.JanusUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserProjectAssociation implements DataClass {
    @JanusUUID("userId")
    private UUID owner;
    @JanusList("projects")
    private List<String> projects = new ArrayList<>();

    public UserProjectAssociation() {
    }

    public UserProjectAssociation(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public List<UUID> getProjects() {
        return projects.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public void addProject(UUID projectId) {
        projects.add(projectId.toString());
    }

    public void removeProject(UUID projectId) {
        projects.remove(projectId.toString());
    }

    @Override
    public String getKey() {
        return owner.toString();
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("userId", owner.toString());
        json.setList("projects", projects.stream().map(o -> (Object) o).toList());
        return json;
    }
}
