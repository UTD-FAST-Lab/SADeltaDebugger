package cs.utd.soles.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReadProcess {

    public static Process process;

    public static String readProcess(String[] commands) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        System.out.println("Starting process for command " + commands);
        Process p = builder.start();
        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            try {
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        p.waitFor();
        //System.out.println("thread output: "+output);
        ReadProcess.process = p;
        return "";
    }

}
