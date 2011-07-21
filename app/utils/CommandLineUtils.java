package utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import play.Logger;

import java.io.IOException;

public class CommandLineUtils {

    public static void runCommand(String command) {
        runCommand(command,true,null);
    }

    public static void runCommand(String command, boolean async) {
        runCommand(command,async,null);
    }

    public static void runCommand(String command, String... args) {
        runCommand(command,true,args);
    }

    public static void runCommand(String command, boolean async, String... args) {
        CommandLine cmd = new CommandLine(command);
        if(args != null)
            cmd.addArguments(args);
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
}
