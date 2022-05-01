package cs.utd.soles.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReadProcess {

    public class ThreadWatcher extends Thread {

        private String output;
        private Process process;
        public String getOutput() {
            return output;
        }

        public ThreadWatcher(Process p) {
            this.output = "";
            this.process = p;
        }

        @Override
        public void run() {
        }
    }
    public Process process;

    public String readProcess(String[] commands) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        System.out.println("Starting process for command " + commands.toString());
        String output = "";
        this.process = builder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
        String line = null;
        try {
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                output += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.process.waitFor();
        return output;
    }

}
