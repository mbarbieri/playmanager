package utils;

import java.io.File;

public class GitUtils {

    public static void cloneRepository(String repository, File destination) {
        CommandLineUtils.runCommand("git clone",false,"--depth=1",repository,destination.getAbsolutePath());
    }
    
}
