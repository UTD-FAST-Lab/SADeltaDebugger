package cs.utd.soles;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.buildphase.BuildScriptRunner;
import cs.utd.soles.buildphase.ProgramWriter;
import cs.utd.soles.reduction.BinaryReduction;
import cs.utd.soles.reduction.HDDReduction;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.testphase.TestScriptRunner;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import java.io.*;
import java.util.*;

public class Runner {

    //1 minute is this long in millis
    private static final long M_TO_MILLIS=60000;

    public static void main(String[] args){
        SetupClass programInfo = new SetupClass();

        setupVariablesToTrack(programInfo);
        programInfo.getPerfTracker().startTimer("program_timer");

        ArrayList<Pair<File,CompilationUnit>> originalCuList = new ArrayList<>();
        ArrayList<Pair<File,CompilationUnit>> bestCuList = new ArrayList<>();

        try{
            programInfo.getPerfTracker().startTimer("setup_timer");
            programInfo.doSetup(args);

            originalCuList=createCuList(programInfo.getRootProjectDir(), programInfo.getJavaParseInst());

            //trackFilesChanges(programInfo,originalCuList);

            System.out.println(programInfo.getArguments().printArgValues());


            if(!BuildScriptRunner.runBuildScript(programInfo)){
                System.out.println("Apk creation failed at start, exiting");
                System.exit(-1);
            }
            //System.out.print("done check");
            saveBestAPK(programInfo);
            programInfo.getPerfTracker().setCount("start_line_count", (int) LineCounter.countLinesDir(programInfo.getRootProjectDir().getAbsolutePath()));

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

        if(!TestScriptRunner.runTestScript(programInfo)) {
            System.out.println("Violation not reproduced");
            System.exit(-1);
        }
        /*try{
            AqlRunner aqlRunner = new AqlRunner(programInfo.getPerfTracker());

            if (aqlRunner.runAql(programInfo, -1, null, -1, "check")) {
                System.out.println("violation reproduced");
            } else {
                System.out.println("Violation not reproduced. Exiting....");
                System.exit(0);
            }
        }catch(Exception e){
            e.printStackTrace();
        }*/


        bestCuList = new ArrayList<>(originalCuList);

        int btimeoutTimeMinutes = 120;
        Optional<Object> arg = programInfo.getArguments().getValueOfArg("BINARY_TIMEOUT_TIME_MINUTES");
        if(arg.isPresent()) {
            btimeoutTimeMinutes= (int) arg.get();
        }
        long beforetime = System.currentTimeMillis();
        BinaryReduction binaryReduction = new BinaryReduction(programInfo,originalCuList, btimeoutTimeMinutes*M_TO_MILLIS);
        arg = programInfo.getArguments().getValueOfArg("CLASS_REDUCTION");
        if(arg.isPresent())
            if(((boolean)arg.get())) {
                ArrayList<Object> requirements = new ArrayList<>();
                requirements.add(originalCuList);
                requirements.add(bestCuList);
                binaryReduction.reduce(requirements);
            }

        long millis_time_saved = Math.max(beforetime+(btimeoutTimeMinutes*M_TO_MILLIS)-System.currentTimeMillis(),0);

        int timeoutTimeMinutes = 120;
        arg = programInfo.getArguments().getValueOfArg("TIMEOUT_TIME_MINUTES");
        if(arg.isPresent()) {
            timeoutTimeMinutes= (int) arg.get();

        }
        HDDReduction hddReduction = new HDDReduction(programInfo, (timeoutTimeMinutes*M_TO_MILLIS)+millis_time_saved);
        arg = programInfo.getArguments().getValueOfArg("REGULAR_REDUCTION");
        if(arg.isPresent())
            if(((boolean)arg.get())) {
                ArrayList<Object> requirements = new ArrayList<>();
                requirements.add(bestCuList);
                hddReduction.reduce(requirements);
            }
        saveBestAPK(programInfo);

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
            saveBestAPK(programInfo);
        }catch(Exception e){
            e.printStackTrace();
        }

        //handle end line count
        try {
            int count = (int) LineCounter.countLinesDir(programInfo.getRootProjectDir().getAbsolutePath());
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

            String filePathName = "debugger/"+programInfo.getThisRunName()+"_time.txt";
            File file = new File(filePathName);
            file.mkdirs();
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(bigString);
            fw.flush();
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

    private static ArrayList<Pair<File,CompilationUnit>> createCuList(File javadirpath, JavaParser parser) throws IOException {

        ArrayList<Pair<File,CompilationUnit>> returnList = new ArrayList<>();

        File f = javadirpath;
        System.out.println("Java dir path: "+ javadirpath);
        if(!f.exists()){
            throw new FileNotFoundException(javadirpath + "not found");
        }

        String[] extensions = {"java"};
        List<File> allJFiles = ((List<File>) FileUtils.listFiles(f, extensions, true));
        int i=0;
        for(File x: allJFiles){
            //don't add the unmodified source files cause they will just duplicate endlessly
            if(!x.getAbsolutePath().contains("unmodified_src")) {
                i++;
                Pair<File, CompilationUnit> b = new Pair(x, parser.parse(x).getResult().get());
                returnList.add(b);

            }
        }

        return returnList;
    }
    //static String APKReductionPath="/home/dakota/AndroidTA/AndroidTAEnvironment/APKReductionDir";

    //this method updates the best apk for this run or creates it if it needs to, by the end of the run the best apk should be saved
    public static void saveBestAPK(SetupClass programInfo){

        try {
            File f= new File("debugger/minimized_apks/" +programInfo.getThisRunName()+".apk");
            f.mkdirs();
            if(f.exists()){
                f.delete();
            }
            f.createNewFile();
            FileUtils.copyFile(programInfo.getAPKFile(), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
