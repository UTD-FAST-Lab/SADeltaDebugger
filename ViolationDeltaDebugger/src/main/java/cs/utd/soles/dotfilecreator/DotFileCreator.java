package cs.utd.soles.dotfilecreator;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.ScriptRunner;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DotFileCreator {
    private static Logger logger = LoggerFactory.getLogger(DotFileCreator.class);
    public static File createDotForProject(SetupClass programInfo, ArrayList<Pair<File, CompilationUnit>> cus){
        programInfo.getPerfTracker().startTimer("jdeps_timer");


        File rootZipDir = turnJarOrApkIntoClassFileDir(programInfo.getAPKFile());


        ArrayList<File> classFilesToGrab = new ArrayList<>();
        for(Pair<File,CompilationUnit> pCu:cus){
            classFilesToGrab.add(Paths.get(rootZipDir.getAbsolutePath()+"/"+pathsForCUs(pCu.getValue0(),pCu.getValue1())).toFile());
        }

        File projectClassesDir = transferClassesToDir(classFilesToGrab,programInfo.getAPKFile());
        //need to find way to get only our projects classes we care about, inolves package name and such



        //need base package name of project
        try {
            String[] command = {"jdeps", "-R", "-verbose", "-dotoutput", projectClassesDir.getAbsolutePath() + "/dotfiles", projectClassesDir.getAbsolutePath()};
            System.out.println("Running command " + Arrays.toString(command));
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process p = pb.start();
            String result = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (p.isAlive())
                while (in.ready()) {
                    result += in.readLine() + "\n";
                }
            p.waitFor();
            programInfo.getPerfTracker().stopTimer("jdeps_timer");
            return Paths.get(projectClassesDir.getAbsolutePath()+"/dotfiles/classes.dot").toFile();
        }catch(Exception e){
            programInfo.getPerfTracker().stopTimer("jdeps_timer");
            e.printStackTrace();
        }
        return null;
    }

    private static File transferClassesToDir(ArrayList<File> projectPackageClasses, File apkDir) {
        //this method turns things into a new directory called classes that is flat.
        File classesDir = new File(apkDir.getAbsolutePath().substring(0,apkDir.getAbsolutePath().lastIndexOf(File.separator))+"/classes");
        for(File x: projectPackageClasses){
            try {
                FileUtils.copyFile(x, new File(classesDir + File.separator + x.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classesDir;
    }

    private static String pathsForCUs(File path, CompilationUnit cu){

        System.out.println("package for cu: "+cu.getPackageDeclaration().get().getNameAsString());
        String packageGuess = cu.getPackageDeclaration().isPresent()? cu.getPackageDeclaration().get().getNameAsString() : "";
        String finalGuess = packageGuess.replace(""+'.',"/")+"/"+path.getName().replace(".java",".class");
        System.out.println("Final guess: "+finalGuess);
        return finalGuess;
    }

    private static File turnJarOrApkIntoClassFileDir(File apkFile) {

        File jarFile = apkFile;

        if(apkFile.getName().contains(".apk")){
            //this is an apk file, first convert it into a jar file
            //command
            String outputFilePath=apkFile.getAbsolutePath().replace(".apk",".jar");

            //dex to jar sh is in AndroidTA_FaultLocalization/resources/delta_debugger/dex-tools-2.1
            // ./d2j-dex2jar.sh -f  "path to apk" -o "outputfile.jar"
            String scriptPath = System.getenv().get("DELTA_DEBUGGER_HOME")+"/dex-tools-2.1/d2j-dex2jar.sh";
            String[] params = new String[] {"-f", apkFile.getAbsolutePath(), "-o", outputFilePath};
            System.out.println("Running command " + scriptPath + Arrays.toString(params));
            try {
                ScriptRunner.runScript(Paths.get(scriptPath).toFile(), params);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //System.out.println(ScriptRunner.lastOutput);
            jarFile= Paths.get(outputFilePath).toFile();
        }
        File zipFile = new File(jarFile.getAbsolutePath().replace(".jar", ".zip"));


        String destUnzipFile = zipFile.getAbsolutePath().replace(".zip","");

        try{
            FileUtils.copyFile(jarFile,zipFile);
            ZipFile src = new ZipFile(zipFile);
            src.extractAll(destUnzipFile);
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Paths.get(destUnzipFile).toFile();
    }
}
