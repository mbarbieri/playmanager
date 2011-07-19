package models;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Files;
import play.libs.IO;

import javax.persistence.Entity;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
        File file = new File(getApplicationPath() + "/server.pid");

        return file.exists();
    }

    public String getStarted() {
        File file = new File(getApplicationPath() + "/server.pid");
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
        Logger.debug("Setup application using file: %s", applicationFile.getName());
        String appsdir = Play.configuration.getProperty("app.appsdirectory");

        File slotDirectory = new File(appsdir + name);
        slotDirectory.mkdir();

        File applicationDirectory = new File(appsdir + name + "/application");
        applicationDirectory.mkdir();

        if (applicationDirectory.exists())
            Files.unzip(applicationFile, applicationDirectory);
    }

    public List<String> status() {
        String playpath = Play.configuration.getProperty("app.playpath");
        String appsdir = Play.configuration.getProperty("app.appsdirectory");

        List<String> output = new ArrayList<String>();
        Logger.debug(playpath + " status " + getApplicationPath());
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(playpath + " status " + getApplicationPath());

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
        execute("stop",false);
    }

    public void deleteDirectory() {
        File file = new File(getApplicationPath());
        if (file.exists())
            Files.deleteDirectory(file);
    }

    public void syncDependencies() {
        execute("deps", "--sync");
    }

    public List<String> showLog() {
        File file = new File(getApplicationPath() + "/logs/system.out");
        if (file.exists())
            return IO.readLines(file);
        return null;
    }

    private void execute(String command, String postfix, boolean async) {
        String playpath = Play.configuration.getProperty("app.playpath");
        CommandLine cmd = new CommandLine(playpath);
        cmd.addArgument(command);
        cmd.addArgument(getApplicationPath());
        cmd.addArguments(postfix);
        DefaultExecutor executor = new DefaultExecutor();
        try {
            if(async) {
                DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                executor.execute(cmd,resultHandler);
            }
            else
                executor.execute(cmd);
            
            Logger.debug("Command: %s", cmd.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getApplicationPath() {
        String appsdir = Play.configuration.getProperty("app.appsdirectory");
        return appsdir + name + "/application";
    }

    private void execute(String command) {
        execute(command, "",true);
    }

    private void execute(String command, boolean async){
        execute(command,"",async);
    }

    private void execute(String command, String postfix){
        execute(command,postfix,true);
    }

    @Override
    public String toString() {
        return name;
    }
}