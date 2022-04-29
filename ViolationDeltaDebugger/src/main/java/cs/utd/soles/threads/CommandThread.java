package cs.utd.soles.threads;


public class CommandThread extends Thread{

    String command="";

    public CommandThread(String command){
        this.command=command;
    }

    public String returnOutput(){
        return output;
    }

    String output="";

    Process process = null;
    @Override
    public void run() {
        try {
            output = ReadProcess.readProcess(command);
            process = ReadProcess.process;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
