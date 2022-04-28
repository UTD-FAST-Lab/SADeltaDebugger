package cs.utd.soles.testphase;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;

public class TestScriptRunner {


    public static boolean runTestScript(SetupClass c){

        //hopefully this test script just prints out true/false
        String command = "./"+c.getTestScriptFile();

        CommandThread testThread = new CommandThread(command);
        testThread.start();
        try {
            testThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return testThread.returnOutput().trim().equalsIgnoreCase("true");

    }
}
