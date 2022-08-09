package cs.utd.soles.othertests;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.LineCounter;
import cs.utd.soles.Runner;
import cs.utd.soles.ScriptRunner;
import cs.utd.soles.setup.ArgsHandler;
import cs.utd.soles.setup.SetupClass;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
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

@SetSystemProperty(key = "DELTA_DEBUGGER_HOME",value = "D:\\Local_androidTAEnvironment\\summer_work\\SADeltaDebugger")
public class OtherTests {

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
    @BeforeClass

    public static void setUpClass(){
        index=0;
        debugHome=System.getProperty("DELTA_DEBUGGER_HOME");
        String f = "src/test/resources/testdata/OtherTests.json";
        argsList = new DebugTest[1];
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
                s.doSetup(argsList[index].args);
                testJsonTest(argsList[index].answers);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void testJsonTest(JSONObject answers) throws IOException, InterruptedException {

        ArrayList<Pair<File, CompilationUnit>> originalCU = new ArrayList<Pair<File,CompilationUnit>>();
        ArrayList<Pair<File, CompilationUnit>> bestCuList = new ArrayList<Pair<File,CompilationUnit>>();



        originalCU = Runner.createCuList(s.getRootProjectDirs(),s.getJavaParseInst());
        assertEquals((long) answers.get("cu_size"),originalCU.size());

        int testbuild = ScriptRunner.runBuildScript(s);
        assertEquals((long) answers.get("test_build"), testbuild);


        long lineCount = 0;
        for(File x: s.getRootProjectDirs()) {
            lineCount += LineCounter.countLinesDir(x.getAbsolutePath());
        }
        assertEquals((long)answers.get("line_counts"),lineCount);
    }
}
