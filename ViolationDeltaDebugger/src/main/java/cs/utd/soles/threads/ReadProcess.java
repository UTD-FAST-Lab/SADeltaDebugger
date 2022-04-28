package cs.utd.soles.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReadProcess {

    public static String readProcess(String[] commands) throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec(commands);


        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String output = "";
        String line = null;

        while((line=r.readLine())!=null){
            output+=line+"\n";
        }
        output+="\n\n";
        while((line=er.readLine())!=null){
            output+=line+"\n";
        }
        p.waitFor();
       // System.out.println("thread output: "+output);
        return output;
    }

    public static String readProcess(String commands) throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec(commands);


        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String output = "";
        String line = null;

        while((line=r.readLine())!=null){
            output+=line+"\n";
        }
        output+="\n\n";
        while((line=er.readLine())!=null){
            output+=line+"\n";
        }
        p.waitFor();
        //System.out.println("thread output: "+output);
        return output;
    }

}
