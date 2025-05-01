package ciclops;

import ciclops.runner.RunnerManager;
import ciclops.security.service.SecurityService;
import common.logger.Logger;
import hades.Hades;

public class Main extends Hades {
    private static final Logger LOGGER = new Logger(Main.class);

    public static void main(String[] args) {
        new Main().startApplication(Main.class);
    }

    @Override
    public void preStart() {
        super.preStart();
        try {
            SecurityService.getInstance().init();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize SecurityService");
            LOGGER.trace(e);
            System.exit(1);
        }
        RunnerManager.getInstance();
    }
}