package cs.utd.soles;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.buildphase.ProgramWriter;
import cs.utd.soles.reduction.BinaryReduction;
import cs.utd.soles.reduction.HDDReduction;
import cs.utd.soles.reduction.MethodReduction;
import cs.utd.soles.setup.ArgsHandler;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.util.SanityException;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.logging.LoggingPermission;

public class Runner {

    private static Logger logger = LoggerFactory.getLogger(Runner.class);
    //1 minute is this long in millis
    private static final long M_TO_MILLIS=60000;

    public static void main(String[] args) throws SanityException {
        ArgsHandler ar = new ArgsHandler();
        new CommandLine(ar).parseArgs(args);
        if (ar.help) {
            CommandLine.usage(new ArgsHandler(), System.out);
            System.exit(1);
        }
        SetupClass programInfo = new SetupClass();

        setupVariablesToTrack(programInfo);
        programInfo.getPerfTracker().startTimer("program_timer");

        ArrayList<Pair<File,CompilationUnit>> originalCuList = new ArrayList<>();
        ArrayList<Pair<File,CompilationUnit>> bestCuList = new ArrayList<>();



        try{
            programInfo.getPerfTracker().startTimer("setup_timer");
            programInfo.doSetup(ar);

            originalCuList=createCuList(programInfo.getRootProjectDirs(), programInfo.getJavaParseInst());


            //trackFilesChanges(programInfo,originalCuList);

            //System.out.println(programInfo.getArguments().printArgValues());
            ProgramWriter.saveCompilationUnits(originalCuList, originalCuList.size()+1,null);

            if(ScriptRunner.runBuildScript(programInfo) != 0) {
                System.out.println("Failed to build program.");
                System.exit(-1);
            }
            //System.out.print("done check");
            //saveBestAPK(programInfo);

            long lineCount = 0;
            for(File x: programInfo.getRootProjectDirs()) {
                lineCount += LineCounter.countLinesDir(x.getAbsolutePath());
            }
            programInfo.getPerfTracker().setCount("start_line_count", (int) lineCount);

            programInfo.getPerfTracker().stopTimer("setup_timer");
            //check if we need to do a minimization
            /*if(!programInfo.isNeedsToBeMinimized()){
             System.out.println("Program doesn't need to be minimized. Exiting...");
                System.exit(0);
            }*/
        }catch(Exception e){
            e.printStackTrace();
        }

        //check if we can reproduce violation

        try {
            if(ScriptRunner.runTestScript(programInfo) != 0) {
                System.out.println("Violation not reproduced");
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        bestCuList = Runner.copyCuList(originalCuList);

        //handle timeout with either default or entered options
        int timeoutTimeMinutes = 120;
        if(ar.timeoutMinutes.isPresent()) {
            timeoutTimeMinutes= ar.timeoutMinutes.get();

        }

        long beforetime = System.currentTimeMillis();
        BinaryReduction binaryReduction = new BinaryReduction(programInfo,originalCuList, (timeoutTimeMinutes)*M_TO_MILLIS);

        if(ar.classReduction) {
            ArrayList<Object> requirements = new ArrayList<>();
            requirements.add(originalCuList);
            requirements.add(bestCuList);
            binaryReduction.reduce(requirements);
            bestCuList=binaryReduction.privateList();
        }

        //after doing binary reduction, calculate time difference

        long newTimeOutMillis = Math.max((beforetime + (timeoutTimeMinutes*M_TO_MILLIS) ) - System.currentTimeMillis(),0);
        System.out.println("CU after BR: "+bestCuList);


        // Do call graph reduction
        beforetime = System.currentTimeMillis();
        MethodReduction methodReduction = new MethodReduction(programInfo, (timeoutTimeMinutes) * M_TO_MILLIS);

        if (ar.methodReduction){
            System.out.println("\n\n\n\n" + methodReduction.findEntryPoint());




        }


        newTimeOutMillis = Math.max((beforetime + (timeoutTimeMinutes*M_TO_MILLIS) ) - System.currentTimeMillis(),0);
        System.out.println("CU after Method: "+bestCuList);
        

        HDDReduction hddReduction = new HDDReduction(programInfo, newTimeOutMillis);
        if(ar.hdd) {
            ArrayList<Object> requirements = new ArrayList<>();
            requirements.add(bestCuList);
            hddReduction.reduce(requirements);
        }
        System.out.println("CU after HDD: "+bestCuList);
        //saveBestAPK(programInfo);

        //doMethodReduction();

        //before we start debugging, sort the pairs based on whos the most dependant
        /*Comparator<Pair<File,CompilationUnit>> cuListComp = new Comparator<Pair<File, CompilationUnit>>() {
            @Override
            public int compare(Pair<File, CompilationUnit> o1, Pair<File, CompilationUnit> o2) {

                ClassNode x1 = matchPair(o1);
                ClassNode x2 = matchPair(o2);

                if(x1 ==null)
                    return 1;
                if(x2==null)
                    return -1;
                if(matchPair(o1).getClosureSize()<matchPair(o2).getClosureSize()){
                    return -1;
                }
                else if(matchPair(o1).getClosureSize()>matchPair(o2).getClosureSize()){
                    return 1;
                }
                return 0;
            }
        };*/

        /**/
        // Collections.sort(bestCUList,cuListComp);


        programInfo.getPerfTracker().stopTimer("program_timer");

        try {
            ProgramWriter.saveCompilationUnits(bestCuList,bestCuList.size()+1,null);
            //final apk for program
            ScriptRunner.runBuildScript(programInfo);
        }catch(Exception e){
            e.printStackTrace();
        }

        //handle end line count
        try {

            int count = 0;
            for(File x: programInfo.getRootProjectDirs()){
                count+=LineCounter.countLinesDir(x.getAbsolutePath());
            }
            programInfo.getPerfTracker().setCount("end_line_count",count);
            String bigString="";
            PerfTracker pt = programInfo.getPerfTracker();
            bigString+="\n"+pt.printNamedValues();
            bigString+="Counts: \n";
            bigString+=pt.printAllCounts();
            bigString+="\nTimes: \n";
            bigString+=pt.printAllTimes();
            bigString+="\nTimers: \n";
            bigString+=pt.printTimerTimes();

            File file = ar.logFile;
            file.mkdirs();
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(bigString);
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //doPrintLog();
    }

    private static void trackFilesChanges(SetupClass programInfo, ArrayList<Pair<File, CompilationUnit>> cuList) {

        for(int i=0;i<cuList.size();i++){
            programInfo.getPerfTracker().addNewCount("cucount_"+cuList.get(i).getValue0().getName()+"_bad_compile");
            programInfo.getPerfTracker().addNewCount("cucount_"+cuList.get(i).getValue0().getName()+"_good_compile");
            programInfo.getPerfTracker().addNewCount("cucount_"+cuList.get(i).getValue0().getName()+"_bad_aql");
            programInfo.getPerfTracker().addNewCount("cucount_"+cuList.get(i).getValue0().getName()+"_good_aql");
        }

    }

    //this method just makes all of the relevant key, value pairs we want to look at.
    private static void setupVariablesToTrack(SetupClass programInfo) {
        PerfTracker p = programInfo.getPerfTracker();

        //specific counts;
        p.addNewCount("start_line_count");
        p.addNewCount("end_line_count");
        p.addNewCount("ast_changes");
        p.addNewCount("bad_compile_runs_binary");
        p.addNewCount("bad_compile_runs_hdd");
        p.addNewCount("good_compile_runs_binary");
        p.addNewCount("good_compile_runs_hdd");
        p.addNewCount("bad_recreate_runs_binary");
        p.addNewCount("bad_recreate_runs_hdd");
        p.addNewCount("good_recreate_runs_binary");
        p.addNewCount("good_recreate_runs_hdd");
        p.addNewCount("total_rotations");
        p.addNewCount("rejected_changes");

        //some timers we need
        p.addNewTimer("compile_timer");
        p.addNewTimer("recreate_timer");
        p.addNewTimer("setup_timer");
        p.addNewTimer("program_timer");
        p.addNewTimer("binary_timer");
        p.addNewTimer("method_timer");
        p.addNewTimer("jdeps_timer");
        p.addNewTimer("hdd_timer");

        //some times we need
        p.addNewTime("time_bad_compile_runs_binary");
        p.addNewTime("time_bad_compile_runs_hdd");
        p.addNewTime("time_good_compile_runs_binary");
        p.addNewTime("time_good_compile_runs_hdd");
        p.addNewTime("time_bad_recreate_runs_binary");
        p.addNewTime("time_bad_recreate_runs_hdd");
        p.addNewTime("time_good_recreate_runs_binary");
        p.addNewTime("time_good_recreate_runs_hdd");

    }

    //gradlew assembleDebug
    /*private static void doMethodReduction(){
        performanceLog.startMethodRedTime();
        /*
         * Method based reduction goes here, right after class based reduction. First, run our modified Flowdroid its in
         * /home/dakota/documents/AndroidTA_FaultLocalization/resources/modified_flowdroid/FlowDroid/soot-infoflow-cmd/target/soot-infoflow-cmd-jar-with-dependencies.jar
         * //TODO:: make this thing an argument, but hard coded works fine. Anyway,
         *
        if(DO_METHOD_REDUCTION) {
            try {
                //create newest version of apk
                synchronized (lockObject) {
                    testerForThis.startApkCreation(projectGradlewPath, projectRootPath, bestCUList,2);
                    lockObject.wait();
                    if (!testerForThis.threadResult) {
                        System.out.println("BUILD FAILED, we didnt change anything so faulty project");
                        System.exit(-1);
                    }
                    saveBestAPK();

                    testerForThis.startCCGProcess(projectAPKPath, thisRunName);
                    lockObject.wait();
                    //our callgraph has now been created so I guess we just should call makeClosures,
                    //and then pass them to a method based reducer


                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        performanceLog.endMethodRedTime();
    }*/

    /*private static void doPrintLog(){
        //log a bunch of information
        try {
            String filePathName = "debugger/java_files/" +thisRunName+"/";
            for (int i = 0; i < bestCUList.size(); i++) {
                File file = new File(filePathName +bestCUList.get(i).getValue0().getName() + ".java");
                file.mkdirs();
                if (file.exists())
                    file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                fw.write(bestCUList.get(i).toString());
                fw.flush();
                fw.close();
            }

            //get the lines count after all changes
            performanceLog.endLineCount=LineCounter.countLinesDir(projectSrcPath);

            filePathName = "debugger/"+thisRunName+"_time.txt";
            File file = new File(filePathName);
            file.mkdirs();
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            long finalRunTimeVar= performanceLog.getProgramRunTime()/1000;
            fw.write("program_runtime: "+finalRunTimeVar+"\n"+"\n");
            fw.write("violation_type: "+targetType+"\n");
            fw.write("violation_or_not: "+violationOrNot+"\n");
            fw.write("setup_time: "+performanceLog.getSetupTime()/1000+"\n");
            fw.write("binary_time:"+performanceLog.getBinaryTime()/1000+"\n"+"\n");

            fw.write("dependency_graph_time: "+performanceLog.getDependencyGraphTime()/1000+"\n");
            fw.write("average_of_good_runtime_aql_binary: " + performanceLog.getAverageOfGoodAQLRuns(0)/1000+"\n");
            fw.write("total_good_aql_runs_binary: "+performanceLog.getTotalAQLRuns(0)+"\n"+"\n");
            fw.write("average_of_good_runtime_compile_binary: " +performanceLog.getAverageOfGoodCompileRuns(0)/1000+"\n");
            fw.write("total_good_compile_runs_binary: "+ performanceLog.getTotalCompileRuns(0)+"\n"+"\n");

            fw.write("average_of_bad_runtime_aql_binary: " + performanceLog.getAverageOfBadAQLRuns(0)/1000+"\n");
            fw.write("total_bad_aql_runs_binary: "+performanceLog.getTotalBadAqlRuns(0)+"\n"+"\n");
            fw.write("average_of_bad_runtime_compile_binary: " +performanceLog.getAverageOfBadCompileRuns(0)/1000+"\n");
            fw.write("total_bad_compile_runs_binary: "+ performanceLog.getTotalBadCompileRuns(0)+"\n"+"\n");

            fw.write("Percent_Of_Program_Time_Taken_By_BinaryReduction: "+((performanceLog.getBinaryTime()/(double)performanceLog.getProgramRunTime())*100)+"\n");
            fw.write("\n"+performanceLog.getPercentagesBinary()+"\n");

            fw.write("average_of_rotations: " + performanceLog.getAverageOfRotations()/1000+"\n");
            fw.write("total_rotations: "+ performanceLog.getTotalRotations()+"\n"+"\n");

            fw.write("average_of_good_runtime_aql_HDD: " + performanceLog.getAverageOfGoodAQLRuns(1)/1000+"\n");
            fw.write("total_good_aql_runs_HDD: "+performanceLog.getTotalAQLRuns(1)+"\n"+"\n");
            fw.write("average_of_good_runtime_compile_HDD: " +performanceLog.getAverageOfGoodCompileRuns(1)/1000+"\n");
            fw.write("total_good_compile_runs_HDD: "+ performanceLog.getTotalCompileRuns(1)+"\n"+"\n");

            fw.write("average_of_bad_runtime_aql_HDD: " + performanceLog.getAverageOfBadAQLRuns(1)/1000+"\n");
            fw.write("total_bad_aql_runs_HDD: "+performanceLog.getTotalBadAqlRuns(1)+"\n"+"\n");
            fw.write("average_of_bad_runtime_compile_HDD: " +performanceLog.getAverageOfBadCompileRuns(1)/1000+"\n");
            fw.write("total_bad_compile_runs_HDD: "+ performanceLog.getTotalBadCompileRuns(1)+"\n"+"\n");
            fw.write("\n"+performanceLog.getPercentagesHDD());
            fw.write("\nnum_candidate_ast: " + testerForThis.candidateCountJava);
            fw.write("\nStart_line_count: "+performanceLog.startLineCount);
            fw.write("\nEnd_line_count: "+performanceLog.endLineCount);
            fw.write("\n%Of_Lines_Removed: "+ ((1.0 - (performanceLog.endLineCount/((double)performanceLog.startLineCount)))*100));
            fwg.write(performanceLog.writeCodeChanges());
            fw.flush();
            fw.close();


            //let the final version of the project_file be the minimized version so we dont have to replace java file manually
            //replace this trash
            testerForThis.saveCompilationUnits(bestCUList);
        }catch(IOException e){
            e.printStackTrace();
        }
    }*/

    /* public static CompilationUnit getASTForFile(String filePath){
            for(Pair<File,CompilationUnit> p:bestCUList){
                if(p.getValue0().getAbsolutePath().equals(filePath))
                    return p.getValue1();

            }
            return null;
    }*/

    public static ArrayList<Pair<File,CompilationUnit>> createCuList(List<File> javadirpaths, JavaParser parser) throws IOException {

        ArrayList<Pair<File,CompilationUnit>> returnList = new ArrayList<>();

        for(File javadirpath:javadirpaths) {
            File f = javadirpath;
            System.out.println("Java dir path: " + javadirpath);
            if (!f.exists()) {
                throw new FileNotFoundException(javadirpath + "not found");
            }

            String[] extensions = {"java"};
            List<File> allJFiles = ((List<File>) FileUtils.listFiles(f, extensions, true));
            System.out.println(allJFiles);
            int i = 0;
            for (File x : allJFiles) {
                //don't add the unmodified source files cause they will just duplicate endlessly
                if (!x.getAbsolutePath().contains("unmodified_src")) {
                    i++;
                    ParseResult<CompilationUnit> parseResult = parser.parse(x);
                    if (parseResult.getResult().isPresent()) {
                        Pair<File, CompilationUnit> b = new Pair(x, parseResult.getResult().get());
                        returnList.add(b);
                    } else {
                        logger.warn(String.format("Parsing failed for file %s", x.toString()));
                    }
                }
            }
        }

        return returnList;
    }
    //static String APKReductionPath="/home/dakota/AndroidTA/AndroidTAEnvironment/APKReductionDir";

    public static ArrayList<Pair<File,CompilationUnit>> copyCuList(ArrayList<Pair<File,CompilationUnit>> originalList){

        ArrayList<Pair<File,CompilationUnit>> returnList = new ArrayList<>();
        for(Pair<File,CompilationUnit> x: originalList){
            Pair<File,CompilationUnit> newS = new Pair<>(x.getValue0(),x.getValue1().clone());
            returnList.add(newS);
        }
        return returnList;
    }


    //this method updates the best apk for this run or creates it if it needs to, by the end of the run the best apk should be saved
    public static void saveBestAPK(SetupClass programInfo) {
        return;
        // Since we are now editing in-place, the best program should just be
        //  the existing one.
//
//        try {
//            File f= new File("debugger/minimized_apks/" +programInfo.getThisRunName()+".apk");
//            f.mkdirs();
//            if(f.exists()){
//                f.delete();
//            }
//            f.createNewFile();
//            FileUtils.copyFile(programInfo.getAPKFile(), f);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    }
}
