package controllers;

import models.Application;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.libs.Files;
import play.mvc.Before;
import play.mvc.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Applications extends Controller {
	
	@Before
	protected static void auth () {
		String username = Play.configuration.getProperty("app.username");
		String password = Play.configuration.getProperty("app.password");
		String user = request.user;
		String pass = request.password;

		if (user == null || password == null || !username.equals(user) || !password.equals(pass))
			unauthorized("playmanager");
	}
	
	public static void index () {
        List<Application> applications = Application.findAll();
		render(applications);
	}
	
	public static void status (String name) {

        Application application = Application.find("byName",name).first();

        List<String> output = application.status();
		
		render(output);
	}
	
	public static void start (String name) {

        Application application = Application.find("byName",name).first();
		if (application != null)
            Logger.info("Starting application %s", application);
            application.start();
		
		index();
	}
	
	public static void stop (String name) {

        Application application = Application.find("byName",name).first();
        if (application != null) {
            Logger.info("Stopping application %s",application);
            application.stop();
        }

		index();
	}
	
	public static void showlog (String name) {

        List<String> output = new ArrayList();
        
        Application application = Application.find("byName",name).first();
        if (application != null) {
            output = application.showLog();
        }
        render(output);
	}
	
	public static void deploy () {
		render();
	}
	
	public static void delete (String name) {
        
        Application application = Application.find("byName",name).first();
		if (application != null) {
            application.stop();
            application.deleteDirectory();
            application.delete();
        }

		index();
	}
	
	public static void deployapp (boolean start, String path, File applicationFile, int deploymentType, String gitPath) {
		if (deploymentType == Application.ZIP && applicationFile == null)
            error("No file uploaded");
        else if(deploymentType == Application.GIT && StringUtils.isBlank(gitPath))
            error("Invalid Git path");

        Application application = new Application();
        application.deploymentType = deploymentType;

        switch(deploymentType) {
            case Application.ZIP:
                String name = applicationFile.getName();
                application.name = name.substring(0, name.indexOf('.'));
                break;
            case Application.GIT:
                application.name = gitPath.substring(gitPath.lastIndexOf("/")+1,gitPath.lastIndexOf("."));
                break;
        }

        application.save();

        Logger.info("Deploying application %s",application.name);
        application.setup(applicationFile, gitPath);

        application.syncDependencies();

		if (start)
			application.start();
		
		index();
	}
	
	public static void redeploy (String name) {
		render(name);
	}
	
	public static void redeployapp(String name, boolean start, boolean keepconf, File applicationFile) {
		if (applicationFile == null)
			index();
		
		String tmpdir = Play.configuration.getProperty("app.appstempdirectory");
		String appsdir = Play.configuration.getProperty("app.appsdirectory");

        Application application = Application.find("byName",name).first();

		//stop running application
		application.stop();

		//unzip new application to tmp folder
		File tmp = new File(tmpdir);
		Files.unzip(applicationFile, tmp);
		
		if (keepconf) {
			//delete conf folder from new application
			Files.deleteDirectory(new File(tmpdir + name + "/conf"));
		
			//copy folder from old application to tmp dir
			Files.copyDir(new File(appsdir + name + "/conf"), new File(tmpdir + name + "/conf"));
		}
		
		//delete old application
        application.deleteDirectory();
        //copy everything from tmp folder to live folder
        Files.copyDir(new File(tmpdir + name), new File(appsdir + name));
        
        //delete tmp folder
        Files.deleteDirectory(new File(tmpdir + name));

        //run dependencies sync
        application.syncDependencies();
        
        if (start)
        	application.start();
        
        index();
	}
	
}
