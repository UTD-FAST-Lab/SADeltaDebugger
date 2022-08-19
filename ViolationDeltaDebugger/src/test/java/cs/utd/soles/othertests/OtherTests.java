package cs.utd.soles.othertests;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.LineCounter;
import cs.utd.soles.Runner;
import cs.utd.soles.ScriptRunner;
import cs.utd.soles.buildphase.ProgramWriter;
import cs.utd.soles.dotfilecreator.DotFileCreator;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OtherTests {

    //holder class that holds run args and answers to each test
    public static class DebugTest{
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
        String f = "src/test/resources/testdata/OtherTests.json";
        Scanner sc = null;
        try {
            sc = new Scanner(new File(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String s ="";
        while(sc.hasNextLine()){
            s += sc.nextLine();
        }
        try {
            JSONArray jb = (JSONArray) new JSONParser().parse(s);
            argsList = new DebugTest[jb.size()];
            for(int i=0;i<jb.size();i++)
                argsList[i] = new DebugTest((JSONObject) jb.get(i));
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
            } catch (IOException | InterruptedException | SanityException e) {
                e.printStackTrace();
            }
        }

    }


    //simulates a run of the delta debugger with answers for each step of the algorithm
    private void testJsonTest(JSONObject answers) throws IOException, InterruptedException, SanityException {

        ArrayList<Pair<File, CompilationUnit>> originalCuList = new ArrayList<Pair<File,CompilationUnit>>();
        ArrayList<Pair<File, CompilationUnit>> bestCuList = new ArrayList<Pair<File,CompilationUnit>>();



        originalCuList = Runner.createCuList(s.getRootProjectDirs(),s.getJavaParseInst());
        assertEquals((long) answers.get("cu_size"),originalCuList.size());

        ProgramWriter.saveCompilationUnits(originalCuList,originalCuList.size()+1,null);

        int testbuild = ScriptRunner.runBuildScript(s);
        assertEquals((long) answers.get("test_build"), testbuild);

        long lineCount = 0;
        for(File x: s.getRootProjectDirs()) {
            lineCount += LineCounter.countLinesDir(x.getAbsolutePath());
        }
        assertEquals((long)answers.get("start_lines"),lineCount);

        int testtest = ScriptRunner.runTestScript(s);
        assertEquals((long) answers.get("test_test"), testtest);


        //cu size, start lines, class diagram, binary reduction, hdd, results


        bestCuList = new ArrayList<>(originalCuList);

        File dotFile = DotFileCreator.createDotForProject(s,originalCuList);
        assertTrue(dotFile.exists());

        BinaryReduction b = new BinaryReduction(s,originalCuList,5*M_TO_MILLIS);
        ArrayList<Object> requirements = new ArrayList<>();
        requirements.add(originalCuList);
        requirements.add(bestCuList);
        b.reduce(requirements);
        bestCuList=b.privateList();

        int afterBinary = bestCuList.size();
        assertEquals((long)answers.get("after_binary"),afterBinary);

        HDDReduction hdd = new HDDReduction(s,10*M_TO_MILLIS);
        requirements = new ArrayList<>();
        requirements.add(bestCuList);
        hdd.reduce(requirements);

        ProgramWriter.saveCompilationUnits(bestCuList,bestCuList.size()+1,null);
        ScriptRunner.runBuildScript(s);

        int endLines = 0;
        for(File x: s.getRootProjectDirs()){
            endLines+=LineCounter.countLinesDir(x.getAbsolutePath());
        }

        assertEquals((long)answers.get("end_lines"),endLines);
        ProgramWriter.saveCompilationUnits(originalCuList,originalCuList.size()+1,null);
        ScriptRunner.runBuildScript(s);
    }
}
