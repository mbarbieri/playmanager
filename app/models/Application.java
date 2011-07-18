package models;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Files;
import play.libs.IO;

import javax.persistence.Entity;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Application extends Model {

    @Required
    public String name;

    public Application() {
    }

    public Application(String name) {
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

    public void setup(File applicationFile) {
        
        String appsdir = Play.configuration.getProperty("app.appsdirectory");
		File target = new File(appsdir);
		if (target.exists())
			Files.unzip(applicationFile, target);
    }

    public List<String> status() {
        String playpath = Play.configuration.getProperty("app.playpath");
        String appsdir = Play.configuration.getProperty("app.appsdirectory");

        List<String> output = new ArrayList<String>();
        Logger.debug(playpath + " status " + appsdir + name);
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(playpath + " status " + appsdir + name);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;

            while ((line = input.readLine()) != null)
                output.add(line);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public void start() {
        execute("start");
    }

    public void stop() {
        execute("stop");
    }

    public void deleteDirectory() {
        String appsdir = Play.configuration.getProperty("app.appsdirectory");
        File file = new File(appsdir + name);
        if (file.exists())
            Files.deleteDirectory(file);
    }

    public void syncDependencies() {
        execute("deps", "--sync", "");
    }

    public List<String> showLog() {
        String appsdir = Play.configuration.getProperty("app.appsdirectory");
        File file = new File(appsdir + name + "/logs/system.out");
        if (file.exists())
            return IO.readLines(file);
        return null;
    }

    private void execute(String command, String postfix, String prefix) {
        String playpath = Play.configuration.getProperty("app.playpath");
        String appsdir = Play.configuration.getProperty("app.appsdirectory");

        try {
            Logger.debug(prefix + " " + playpath + " " + command + " " + appsdir + name + " " + postfix);
            Runtime.getRuntime().exec(prefix + " " + playpath + " " + command + " " + appsdir + name + " " + postfix);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void execute(String command) {
        execute(command, "", "");
    }

    @Override
    public String toString() {
        return name;
    }
}