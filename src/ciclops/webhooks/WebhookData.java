package ciclops.webhooks;

import dobby.util.json.NewJson;

import java.util.UUID;

public class WebhookData {
    private String projectName;
    private String projectKey;
    private boolean success;
    private UUID runnerId;
    private String errorMessage;
    private boolean isReleaseBuild;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public UUID getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(UUID runnerId) {
        this.runnerId = runnerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isReleaseBuild() {
        return isReleaseBuild;
    }

    public void setReleaseBuild(boolean releaseBuild) {
        isReleaseBuild = releaseBuild;
    }

    public NewJson toJson() {
        NewJson json = new NewJson();
        json.setString("projectName", projectName);
        json.setString("projectKey", projectKey);
        json.setBoolean("success", success);
        json.setString("runnerId", runnerId != null ? runnerId.toString() : null);
        json.setString("errorMessage", errorMessage != null ? errorMessage : "");
        json.setBoolean("isReleaseBuild", isReleaseBuild);
        return json;
    }
}
