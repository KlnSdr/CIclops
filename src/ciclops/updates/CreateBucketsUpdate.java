package ciclops.updates;

import ciclops.projects.service.ProjectsService;
import ciclops.runner.service.BuildProcessLogService;
import hades.update.Update;
import thot.connector.Connector;

public class CreateBucketsUpdate implements Update {
    @Override
    public boolean run() {
        final String[] buckets = {ProjectsService.BUCKET_NAME, ProjectsService.USER_PROJECT_ASSOCIATION_BUCKET_NAME, BuildProcessLogService.BUCKET_NAME};

        for (String bucket : buckets) {
            if (!(Connector.write(bucket, "TMP", true) && Connector.delete(bucket, "TMP"))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "ciclops_create-buckets";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_BUCKETS.getOrder();
    }
}
