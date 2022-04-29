package cs.utd.soles.testphase;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;

public class TestScriptRunner {


    public static boolean runTestScript(SetupClass c){

        //hopefully this test script just prints out true/false
        String command = c.getTestScriptFile().toString();

        CommandThread testThread = new CommandThread(command);
        testThread.start();
        try {
            testThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Output of test script was " + testThread.returnOutput());
        System.out.println("Exit code of running test script was " + testThread.process.exitValue());
        return testThread.process.exitValue()== 0;
    }
}
