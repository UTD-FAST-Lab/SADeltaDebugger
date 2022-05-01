package cs.utd.soles;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ScriptRunner {

    public static boolean runBuildScript(SetupClass sc) {
        return runScript(sc.getBuildScriptFile());
    }

    public static boolean runTestScript(SetupClass sc) {
        return runScript(sc.getTestScriptFile(), new String[] {sc.getAPKFile().toString()});
    }

    public static boolean runScript(File scriptLocation) {
        return ScriptRunner.runScript(scriptLocation, new String[] {});
    }

    /**
     * Runs a script with params.
     * @param scriptLocation The location of the script to run.
     * @param params Any parameters to run the script with.
     * @return
     */
    public static boolean runScript(File scriptLocation, String[] params) {

        //hopefully this test script just prints out true/false
        List<String> command = new ArrayList<String>();
        command.add(scriptLocation.toString());
        Collections.addAll(command, params);
        CommandThread testThread = new CommandThread(command.toArray(new String[] {}));
        testThread.start();
        try {
            testThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Output of script was " + testThread.returnOutput());
        System.out.println("Exit code of running build script was " + testThread.process.exitValue());
        return testThread.process.exitValue() == 0;
    }
}
