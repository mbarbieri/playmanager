package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.Application;

import play.Play;
import play.libs.Files;
import play.libs.IO;
import play.mvc.Before;
import play.mvc.Controller;

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
		String appsdir = Play.configuration.getProperty("app.appsdirectory");
		List<Application> applications = new ArrayList(); 
		
		File dir = new File(appsdir);
		if (dir.exists()) {
			List<File> folders = new ArrayList(Arrays.asList(dir.listFiles())); 
			
			for (File file : folders) {
				if (file.isDirectory() && !file.getName().equals("tmp")) {
					Application application = new Application();
					application.setName(file.getName());
					applications.add(application);
				}
			}
		}
		
		render(applications);
	}
	
	public static void status (String name) {
		String playpath = Play.configuration.getProperty("app.playpath");
		String appsdir = Play.configuration.getProperty("app.appsdirectory");
		List<String> output = new ArrayList<String>();

		try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(playpath + " status " + appsdir + name);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            
            while ((line = input.readLine()) != null)
            	output.add(line);

        } catch(Exception e) {
        	 System.out.println(e.toString());
        }
		
		render(output);
	}
	
	public static void start (String name) {
		if (name != null)
			execute("start", name, "", "");
		
		index();
	}
	
	public static void stop (String name) {
		if (name != null)
			execute("stop", name, "", "");
		
		index();
	}
	
	public static void showlog (String name) {
		List<String> output = new ArrayList();
		
		if (name != null) {
			String appsdir = Play.configuration.getProperty("app.appsdirectory");
			File file = new File (appsdir + name + "/logs/system.out");
			if (file.exists())
				output = IO.readLines(file);
		}
		
		render(output);
	}	
	
	public static void deploy () {
		render();
	}
	
	public static void delete (String name) {
		if (name != null) {
			execute("stop", name, "", "");
		
			String appsdir = Play.configuration.getProperty("app.appsdirectory");
			File file = new File(appsdir + name);
			if (file.exists())
				Files.deleteDirectory(file);
		}
		
		index();
	}
	
	public static void deployapp (boolean start, String path, File application) {
		String name = application.getName();
		String fileName = name.substring(0, name.indexOf('.'));
		String appsir = Play.configuration.getProperty("app.appsdirectory");

		File target = new File(appsir);
		if (target.exists())
			Files.unzip(application, target);	
		
		execute("deps", fileName, "--sync", "");
		
		if (start)
			execute("start", fileName, "", "");
		
		index();
	}
	
	public static void redeploy (String name) {
		render(name);
	}
	
	public static void redeployapp(String name, boolean start, File application) {
		String tmpdir = Play.configuration.getProperty("app.appstempdirectory");
		String appsdir = Play.configuration.getProperty("app.appsdirectory");
		
		//stop running application
		execute("stop", name, "", "");
		
		//unzip new application to tmp folder
		File tmp = new File(tmpdir);
		Files.unzip(application, tmp);
		
		//delete conf folder from new application
		Files.deleteDirectory(new File(tmpdir + name + "/conf"));
		
		//copy folder from old application to tmp dir
		Files.copyDir(new File(appsdir + name + "/conf"), new File(tmpdir + name + "/conf"));
		
		//delete old application
        Files.deleteDirectory(new File(appsdir + name));
        
        //copy everything from tmp folder to live folder
        Files.copyDir(new File(tmpdir + name), new File(appsdir + name));
        
        //delete tmp folder
        Files.deleteDirectory(new File(tmpdir + name));

        //run dependencies sync
        execute("deps", name, "--sync", ""); 
        
        if (start)
        	execute("start", name, "", "");   
        
        index();
	}
	
	private static void execute (String command, String name, String postfix, String prefix) {
		String playpath = Play.configuration.getProperty("app.playpath");
		String appsdir = Play.configuration.getProperty("app.appsdirectory");
		
		try {
            Runtime.getRuntime().exec(prefix + " " + playpath + " " + command + " " + appsdir + name + " " + postfix);
        } catch(Exception e) {
            System.out.println(e.toString());
        }
	}	
}