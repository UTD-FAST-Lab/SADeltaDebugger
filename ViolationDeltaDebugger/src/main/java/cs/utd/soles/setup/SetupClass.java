package cs.utd.soles.setup;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import cs.utd.soles.PerfTracker;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class SetupClass {


    public boolean isViolationOrNot() {
        return violationOrNot;
    }

    public String getThisRunName() {
        return thisRunName;
    }

    public PerfTracker getPerfTracker(){
        return this.performance;
    }

    public ParserConfiguration getParserConfig() {
        return parserConfig;
    }

    public JavaParser getJavaParseInst() {
        return javaParseInst;
    }


    public File getRootProjectDir() {
        return rootProjectDir;
    }

    public File getBuildScriptFile() {
        return buildScriptFile;
    }

    public File getTestScriptFile() {
        return testScriptFile;
    }

    public File getAPKFile(){
        return apkFile;
    }

    File rootProjectDir;
    boolean violationOrNot;
    ArgsHandler arguments;
    String thisRunName;
    PerfTracker performance;
    ParserConfiguration parserConfig;
    JavaParser javaParseInst;
    File buildScriptFile;
    File testScriptFile;
    File apkFile;

    /*
    * Setup is a couple of steps.
    * 1. we need a schema
    * 2. we need a project to work on
    * 3. we need to know where that project is
    * */

    public SetupClass(){
        performance = new PerfTracker();
        parserConfig = new ParserConfiguration();

    }
    public boolean doSetup(String[] args) throws IOException {

        //positionals
        //0 root
        //1 apk file
        //2 buildscript
        //3 test

        rootProjectDir=Paths.get(args[0]).toFile();
        apkFile=Paths.get(args[1]).toFile();
        buildScriptFile=Paths.get(args[2]).toFile();
        testScriptFile=Paths.get(args[3]).toFile();


        arguments = handleArgs(args);
        javaParseInst = new JavaParser(parserConfig);
        return true;
    }



    private ArgsHandler handleArgs(String[] args) {

        return new ArgsHandler(args);
    }


    public ArgsHandler getArguments() {
        return arguments;
    }

}



