package utils;

import models.Application;
import play.Play;

public class PlayUtils {

    private static String getPlayPath() {
        return Play.configuration.getProperty("app.playpath");
    }

    private static String getAppsDirectory() {
        return Play.configuration.getProperty("app.appsdirectory");
    }

    public static void play(String command, Application application, boolean async, String... args) {
        CommandLineUtils.runCommand(getPlayPath() + " " + command + " " +
                getAppsDirectory() + application.name + "/application", async, args);
    }

    public static void play(String command, Application application, String... args) {
        play(command,application,true,args);
    }

    public static void play(String command, Application application) {
        play(command, application, null);
    }

    public static void start(Application application) {
        play("start", application);
    }

    public static void stop(Application application) {
        play("stop", application, false);
    }

    public static void syncDependencies(Application application) {
        play("deps",application,"--sync");
    }
}
