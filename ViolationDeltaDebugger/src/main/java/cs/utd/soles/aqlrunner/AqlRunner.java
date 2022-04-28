/*package cs.utd.soles.aqlrunner;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.PerfTracker;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;
import cs.utd.soles.threads.ReadProcess;
import org.javatuples.Pair;

import javax.xml.stream.events.Comment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AqlRunner{

    PerfTracker pTracker;
    private SetupClass setupInfo;
    private static int posChanged;
    private static ArrayList<Pair<File, CompilationUnit>> list;
    public AqlRunner(PerfTracker pT) {
        this.pTracker=pT;
    }

   /* @Override
    public void handleThread(Thread thread, ProcessType type, String finalString, String finalString2) {
        switch(type){
            case AQL_RUN:
                synchronized(lockObject){
                    //final results of aql are in both finalString1 and finalString2 respectively
                    //order is config1, config2
                    pTracker.stopTimer("aql_timer");
                    threadResult=AQLStringHandler.handleAQL(setupInfo,finalString,finalString2);
                    if(threadResult){
                        //good one
                        incrementCorrectCount(thread,true);

                    }else{
                        //failed one
                        incrementCorrectCount(thread,false);
                    }
                    setupInfo=null;
                    pTracker.resetTimer("aql_timer");
                    lockObject.notify();
                }
                break;
        }
    }


    public boolean runAql(SetupClass info, int caller, ArrayList<Pair<File, CompilationUnit>> cuListToTest, int posChanged, String changeNum) throws IOException {
        setupInfo=info;

        //this bit runs and captures the output of the aql script
        String command1 = "python runaql.py "+info.getConfig1()+" "+info.getTargetProject().getProjectAPKPath()+" -f";
        String command2 = "python runaql.py "+info.getConfig2()+" "+info.getTargetProject().getProjectAPKPath()+" -f";
        list=cuListToTest;
        AqlRunner.posChanged =posChanged;

        boolean runaql1=true;
        boolean runaql2=true;
        //if its a violation always run both
        if(info.isViolationOrNot()){
            runaql1=true;
            runaql2=true;
        }else{
            if(info.getThisViolation().getConfig1_FlowList().size()==0)
                runaql1=false;
            if(info.getThisViolation().getConfig2_FlowList().size()==0)
                runaql2=false;
        }

        //start these commands and then handle them somewhere else

        pTracker.startTimer("aql_timer");

        CommandThread command1T = new CommandThread(command1);
        CommandThread command2T = new CommandThread(command2);

        if(runaql1){
            command1T.start();
        }
        if(runaql2){
            command2T.start();
        }
        try {
            command1T.join();
            command2T.join();

            return checkOutput(command1T.returnOutput(),command2T.returnOutput(),caller,changeNum);



        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private void incrementCorrectCount(boolean passOrFail, int caller) {

        String correctName = passOrFail?"good_aql_runs_":"bad_aql_runs_";
        switch(caller){
            case 0:
                correctName+="binary";
                pTracker.addCount(correctName,1);
                correctName="time_"+correctName;
                pTracker.addTime(correctName,pTracker.getTimeForTimer("aql_timer"));
                break;
            case 1:
                correctName+="hdd";
                pTracker.addCount(correctName,1);
                correctName="time_"+correctName;
                pTracker.addTime(correctName,pTracker.getTimeForTimer("aql_timer"));

                if(posChanged>-1&&posChanged<list.size()) {
                    correctName=list.get(posChanged).getValue0().getName();
                    correctName="cucount_"+correctName+(passOrFail?"_good_aql":"_bad_aql");
                    pTracker.addCount(correctName, 1);
                }
                break;
            default:
                break;
        }

    }

    private boolean checkOutput(String finalString, String finalString2, int caller,String changeNum){
        //final results of aql are in both finalString1 and finalString2 respectively
        //order is config1, config2
        pTracker.stopTimer("aql_timer");
        boolean result=AQLStringHandler.handleAQL(setupInfo,finalString,finalString2,changeNum);
        if(result){
            //good one
            incrementCorrectCount(true,caller);

        }else{
            //failed one
            incrementCorrectCount(false,caller);
        }
        setupInfo=null;
        pTracker.resetTimer("aql_timer");
        return result;
    }
}
*/