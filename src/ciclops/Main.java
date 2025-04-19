package ciclops;

import ciclops.runner.RunnerManager;
import hades.Hades;

public class Main extends Hades {
    public static void main(String[] args) {
        new Main().startApplication(Main.class);
    }

    @Override
    public void preStart() {
        super.preStart();
        RunnerManager.getInstance();
    }
}