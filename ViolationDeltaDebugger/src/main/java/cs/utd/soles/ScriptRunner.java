package cs.utd.soles;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ScriptRunner {

    public static int runBuildScript(SetupClass sc) throws IOException, InterruptedException {
        int exitCode = runScript(sc.getBuildScriptFile());
        System.out.println(String.format("Output of running build script was %d", exitCode));
        return exitCode;
    }

    public static int runTestScript(SetupClass sc) throws IOException, InterruptedException {
        int exitCode = runScript(sc.getTestScriptFile(), new String[] {sc.getAPKFile().toString()});
        System.out.println(String.format("Output of running violation script was %d", exitCode));
        return exitCode;
    }

    public static int runScript(File scriptLocation) throws IOException, InterruptedException {
        return ScriptRunner.runScript(scriptLocation, new String[] {});
    }

    /**
     * Runs a script with params.
     * @param scriptLocation The location of the script to run.
     * @param params Any parameters to run the script with.
     * @return
     */
    public static int runScript(File scriptLocation, String[] params) throws IOException, InterruptedException {

        //hopefully this test script just prints out true/false
        List<String> command = new ArrayList<String>();
        command.add(scriptLocation.toString());
        Collections.addAll(command, params);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(scriptLocation.getParent()));
        pb.redirectErrorStream(true);
        String output = "";
        Process process = pb.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        try {
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                output += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        process.waitFor();
        lastOutput = output;
        //System.err.println("Output of script was " + output);
        //System.err.println("Exit code of running build script was " + process.exitValue());
        return process.exitValue();
    }
    public static String lastOutput;
}
