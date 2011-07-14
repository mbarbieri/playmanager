package models;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;

import javax.persistence.Entity;

import play.Play;
import play.db.jpa.Model;

public class Application {
	public String name;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean isRunning() {
		String appsdir = Play.configuration.getProperty("app.appsdirectory");
		File file = new File(appsdir + name + "/server.pid");
		
		return file.exists();
	}
	
	public String getStarted() {
		File file = new File(Play.configuration.getProperty("app.appsdirectory") + name + "/server.pid");
		
		if (file.exists()) {
			long started = file.lastModified();
			
			if (started > 0) {
				Format formatter = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
				return formatter.format(started);
			}
		}
		
		return "-";
	}
}