package cs.utd.soles.setup;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import cs.utd.soles.PerfTracker;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

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


    public ArrayList<File> getRootProjectDirs() {
        return rootProjectDirs;
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

    ArrayList<File> rootProjectDirs;
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
    public boolean doSetup(ArgsHandler ar) throws IOException {

        //positionals
        rootProjectDirs = new ArrayList<>();
        javaParseInst = new JavaParser(parserConfig);

        thisRunName=ar.runPrefix+this.rootProjectDirs.get(0).getAbsolutePath().replace(File.separator,"-");
        return true;
    }

    public void addSrcDir(File srcFile){
        rootProjectDirs.add(srcFile);
    }
    public void setApkFile(File apkFile)
    {
        this.apkFile = apkFile;
    }
    public void setTestScriptFile(File testSFile){
        this.testScriptFile=testSFile;
    }
    public void setBuildScriptFile(File buildSFile){
        this.buildScriptFile=buildSFile;
    }

    public ArgsHandler getArguments() {
        return arguments;
    }

}



