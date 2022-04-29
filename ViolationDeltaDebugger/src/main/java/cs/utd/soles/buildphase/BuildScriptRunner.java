package cs.utd.soles.buildphase;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;

public class BuildScriptRunner {

    public static boolean runBuildScript(SetupClass c){

        //hopefully this test script just prints out true/false
        String command = c.getBuildScriptFile().toString();
        System.out.println(command);
        CommandThread testThread = new CommandThread(command);
        testThread.start();
        try {
            testThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Exit code of running build script was " + testThread.process.exitValue());
        return testThread.process.exitValue() == 0;

    }
}
