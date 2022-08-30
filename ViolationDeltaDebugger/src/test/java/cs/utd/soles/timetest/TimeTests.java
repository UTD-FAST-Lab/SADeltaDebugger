package cs.utd.soles.timetest;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.LineCounter;
import cs.utd.soles.Runner;
import cs.utd.soles.ScriptRunner;
import cs.utd.soles.buildphase.ProgramWriter;
import cs.utd.soles.reduction.BinaryReduction;
import cs.utd.soles.reduction.HDDReduction;
import cs.utd.soles.setup.ArgsHandler;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.util.SanityException;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import picocli.CommandLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class TimeTests {

    //holder class that holds run args and answers to each test
    static class DebugTest{
        ArgsHandler args;
        JSONObject answers;
        String name;

        public DebugTest(String name, ArgsHandler args, JSONObject answers){
            this.name=name;
            this.args=args;
            this.answers=answers;
        }
        public DebugTest(JSONObject obj){
            this.name= (String) obj.get("test_name");
            this.answers= (JSONObject) obj.get("test_answers");
            this.args=new ArgsHandler();

            JSONArray stuff = (JSONArray) obj.get("test_args");

            String[] strings = new String[stuff.size()];
            for(int i=0;i<stuff.size();i++){
                strings[i]=(String)stuff.get(i);
            }
            new CommandLine(this.args).parseArgs(strings);
        }
    }
    static DebugTest[] argsList;
    static int index=0;
    static String debugHome;
    private static final long M_TO_MILLIS=60000;
    @BeforeClass

    public static void setUpClass(){
        index=0;
        debugHome=System.getProperty("DELTA_DEBUGGER_HOME");
        String f = "src/test/resources/testdata/TimeTests.json";
        Scanner sc = null;
        try {
            sc = new Scanner(new File(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String s ="";;
        while(sc.hasNextLine()){
            s += sc.nextLine();
        }
        try {
            JSONArray jb = (JSONArray) new JSONParser().parse(s);
            argsList = new DebugTest[jb.size()];
            for(int i=0;i<jb.size();i++)
                argsList[i] = new DebugTest((JSONObject) jb.get(i));
            System.out.println(argsList[0].args.toString());
            System.out.println(argsList[0].args.timeoutMinutes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //read in a json of args and answers


    }

    public SetupClass s;

    @After
    public void after(){

    }

    @Test
    public void testAllJsonTests(){


        for(;index<argsList.length;index++){

            try {
                s = new SetupClass();
                ScriptRunner.setBSanityCheck(-2);
                s.doSetup(argsList[index].args);
                testJsonTest(argsList[index].answers);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    //simulates a run of the delta debugger with answers for each step of the algorithm
    private void testJsonTest(JSONObject answers) throws IOException, InterruptedException {

        ArrayList<Pair<File, CompilationUnit>> originalCuList = new ArrayList<Pair<File,CompilationUnit>>();
        ArrayList<Pair<File, CompilationUnit>> bestCuList = new ArrayList<Pair<File,CompilationUnit>>();

        originalCuList = Runner.createCuList(s.getRootProjectDirs(),s.getJavaParseInst());
        bestCuList = Runner.copyCuList(originalCuList);

        ScriptRunner.runBuildScript(s);
        int timeoutTimeMinutes = 120;
        if(s.getArguments().timeoutMinutes.isPresent()) {
            timeoutTimeMinutes= s.getArguments().timeoutMinutes.get();

        }
        long beforetime = System.currentTimeMillis();
        BinaryReduction b = new BinaryReduction(s,originalCuList,timeoutTimeMinutes*M_TO_MILLIS);
        ArrayList<Object> requirements = new ArrayList<>();
        requirements.add(originalCuList);
        requirements.add(bestCuList);
        b.reduce(requirements);
        bestCuList=b.privateList();

        long newTimeOutMillis = Math.max((beforetime + (timeoutTimeMinutes*M_TO_MILLIS) ) - System.currentTimeMillis(),0);

        HDDReduction hdd = new HDDReduction(s,newTimeOutMillis);
        requirements = new ArrayList<>();
        requirements.add(bestCuList);
        try {
            hdd.reduce(requirements);
        } catch (SanityException e) {
            e.printStackTrace();
        }

        long operationTime = System.currentTimeMillis()-beforetime;

        long stoptime = beforetime+(((long)answers.get("program_timer")*M_TO_MILLIS))+300;

        System.out.println("reverted units");
        ProgramWriter.saveCompilationUnits(originalCuList,originalCuList.size()+1,null);
        ScriptRunner.runBuildScript(s);
        assertTrue(operationTime < stoptime);
    }
}
