package controllers;

import models.Application;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matteo Barbieri <barbieri.matteo@gmail.com>
 */
@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() throws Exception {
        String appsdir = Play.configuration.getProperty("app.appsdirectory");

        File dir = new File(appsdir);
        if (dir.exists() && dir.isDirectory()) {
            List<File> folders = new ArrayList(Arrays.asList(dir.listFiles()));

            for (File file : folders) {
                Application applicationFound = Application.find("byName",file.getName()).first();
                if (applicationFound == null && file.isDirectory() && !file.getName().equals("tmp") && !file.getName().equals("playmanager")) {
                    Logger.info("Added application %s",file.getName());
                    Application application = new Application(file.getName());
                    File gitDirectory = new File(file.getAbsolutePath()+"/application/.git");
                    if(gitDirectory.exists())
                        application.deploymentType = Application.GIT;
                    else
                        application.deploymentType = Application.ZIP;
                    application.save();
                }
            }
        }

    }
}
