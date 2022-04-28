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

    @Override
    public void run() {
        try {
            output = ReadProcess.readProcess(command);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
