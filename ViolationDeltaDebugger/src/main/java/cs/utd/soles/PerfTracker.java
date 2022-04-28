package cs.utd.soles;


import org.apache.commons.lang3.time.StopWatch;
import java.util.HashMap;
import java.util.Map;

public class PerfTracker {

    private HashMap<String,Long> namedTimes;
    private HashMap<String,StopWatch> namedTimers;
    private HashMap<String,Integer> namedCounts;
    private HashMap<String, String> namedValues;
    public PerfTracker(){
        namedTimes = new HashMap<>();
        namedTimers = new HashMap<>();
        namedCounts = new HashMap<>();
        namedValues = new HashMap<>();
    }

    //add a named timer
    public boolean addNewTimer(String name){

        if(namedTimers.containsKey(name)){
            return false;
        }
        namedTimers.put(name, new StopWatch());
        return true;
    }

    //start a timer
    public boolean startTimer(String name){
        StopWatch timer = namedTimers.get(name);
        if(timer==null){
            return false;
        }
        timer.start();
        return true;
    }

    //stop a timer
    public boolean stopTimer(String name){
        StopWatch timer = namedTimers.get(name);
        if(timer==null){
            return false;
        }
        timer.stop();
        return true;
    }

    //pause a timer
    public boolean pauseTimer(String name){
        StopWatch timer = namedTimers.get(name);
        if(timer==null){
            return false;
        }
        timer.suspend();
        return true;
    }

    //resume a timer
    public boolean unpauseTimer(String name){
        StopWatch timer = namedTimers.get(name);
        if(timer==null){
            return false;
        }
        timer.resume();
        return true;
    }

    //reset a timer
    public boolean resetTimer(String name){
        StopWatch timer = namedTimers.get(name);
        if(timer==null){
            return false;
        }
        timer.reset();
        return true;
    }


    public int getCountForCount(String name){
        return namedCounts.get(name);
    }
    //gets a timers time
    public long getTimeForTimer(String name){
        StopWatch timer = namedTimers.get(name);
        if(timer==null){
            return -1;
        }
        return timer.getTime();
    }

    //add a new time to track
    public boolean addNewTime(String name){
        if(namedTimes.containsKey(name)){
            return false;
        }
        namedTimes.put(name, 0L);
        return true;
    }

    //set time to track
    public boolean setTime(String name, long time){
        if(!namedTimes.containsKey(name)){
            return false;
        }
        namedTimes.put(name, time);
        return true;
    }

    //add time to a time
    public boolean addTime(String name, long time){
        if(!namedTimes.containsKey(name)){
            return false;
        }
        namedTimes.put(name, time+namedTimes.get(name));
        return true;
    }

    //add a new count to track
    public boolean addNewCount(String name){
        if(namedCounts.containsKey(name)){
            return false;
        }
        namedCounts.put(name, 0);
        return true;
    }

    //set time to track
    public boolean setCount(String name, int count){
        if(!namedCounts.containsKey(name)){
            return false;
        }
        namedCounts.put(name, count);
        return true;
    }



    //add time to a time
    public boolean addCount(String name, int count){
        if(!namedCounts.containsKey(name)){
            return false;
        }
        namedCounts.put(name, count+namedCounts.get(name));
        return true;
    }

    public boolean setNamedValue(String name, String value){
        namedValues.put(name,value);
        return true;
    }

    //prints values
    public String printNamedValues(){
        String returnString="";
        for(Map.Entry<String, String> e:namedValues.entrySet()){
            returnString += e.getKey().toString()+": "+e.getValue()+"\n";
        }
        return returnString;
    }

    //prints stuff for timers
    public String printTimerTimes(){
        String returnString="";
        for(Map.Entry<String, StopWatch> e:namedTimers.entrySet()){
            returnString += e.getKey().toString()+": "+e.getValue().getTime()/1000+"\n";
        }
        return returnString;
    }

    //prints stuff for times
    public String printAllTimes(){
        String returnString="";
        for(Map.Entry<String, Long> e:namedTimes.entrySet()){
            returnString += e.getKey().toString()+": "+e.getValue()/1000+"\n";
        }
        return returnString;
    }

    //prints stuff for counts
    public String printAllCounts(){
        String returnString="";
        for(Map.Entry<String, Integer> e:namedCounts.entrySet()){
            returnString += e.getKey().toString()+": "+e.getValue()+"\n";
        }
        return returnString;
    }
}
