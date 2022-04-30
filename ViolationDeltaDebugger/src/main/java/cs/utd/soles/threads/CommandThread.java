package cs.utd.soles.threads;



public class CommandThread extends Thread{

    String[] command = new String[] {};

    public CommandThread(String[] command){
        this.command=command;
    }

    public String returnOutput(){
        return output;
    }

    String output="";

    public Process process = null;
    @Override
    public void run() {
        try {
            ReadProcess rp = new ReadProcess();
            output = rp.readProcess(command);
            this.process = rp.process;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
