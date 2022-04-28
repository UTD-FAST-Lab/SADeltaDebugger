package cs.utd.soles.dotfilecreator;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DotFileCreator {
    public static File createDotForProject(SetupClass programInfo, ArrayList<Pair<File, CompilationUnit>> cus){
        programInfo.getPerfTracker().startTimer("jdeps_timer");


        File rootZipDir = turnJarOrApkIntoClassFileDir(programInfo.getAPKFile());


        File projectPackageClasses= findProjectClasses(rootZipDir,cus);

        File projectClassesDir = transferClassesToDir(projectPackageClasses,programInfo.getAPKFile());



        //need to find way to get only our projects classes we care about, inolves package name and such
        //need base package name of project
        try {
            String[] command = {"jdeps", "-R", "-verbose", "-dotoutput", projectClassesDir.getAbsolutePath() + "/dotfiles", projectClassesDir.getAbsolutePath()};

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

    private static File transferClassesToDir(File projectPackageClasses, File apkDir) {
        //this method turns things into a new directory called classes that is flat.
        File classesDir = new File(apkDir.getAbsolutePath().substring(0,apkDir.getAbsolutePath().lastIndexOf(File.separator))+"/classes");
        String[] extension= {"class"};
        List<File> listFiles = (List<File>) FileUtils.listFiles(projectPackageClasses,extension,true);

        for(File x: listFiles){
            try {
                FileUtils.copyFile(x, new File(classesDir + File.separator + x.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classesDir;
    }

    private static File findProjectClasses(File rootZipDir, ArrayList<Pair<File, CompilationUnit>> cus) {

        ///so this method will cycle through the packages/classnames is rootZipDir and try to find the directory that contains the .class
        //files that match up to our .java files
       /* String[] extensions = {"class"};
        List<File> fileList = (List<File>) FileUtils.listFiles(rootZipDir,extensions,true);


        for(File example:fileList){
        }*/

       //rootProject of our dir is longestCommonSubstring of our ArrayList<Pair<File,sss>> cu
        //but they all start the same so dw bout doin the actual problem
        String path = "";
        ArrayList<String> pathToFiles = new ArrayList<>();
        int bigSize=0;
        int bigPos=0;
        for(int i=0;i<cus.size();i++){
            String xpath=cus.get(i).getValue0().getAbsolutePath();
            pathToFiles.add(cus.get(i).getValue0().getAbsolutePath());
            if(xpath.length()>bigSize) {
                bigSize = xpath.length();
                bigPos=i;
            }

        }
        boolean done=false;
        for(int i=0;i<bigSize&&!done;i++){
            char c = pathToFiles.get(bigPos).charAt(i);
            for(String x:pathToFiles){
                if(x.charAt(i)!=c){
                    done=true;
                    break;
                }
            }
            if(!done)
                path+=c;
        }
        System.out.println("path we like: "+path);

        //path bla bla bla
        String[] split = path.split(File.separator);
        int i=0;
        for(String x: split){
            if(x.isEmpty()){
                i++;
                continue;
            }
            System.out.println(rootZipDir+File.separator+x +" : " + Paths.get(rootZipDir+File.separator+x).toFile().exists());
            if(Paths.get(rootZipDir+File.separator+x).toFile().exists()) {

                break;
            }
            i++;
        }
        String bestGuess=rootZipDir.getAbsolutePath()+File.separator;
        int cutoff=split.length;
        if(split[cutoff-1].contains(".java"))
            cutoff=cutoff-1;
        for(int j=i;j<cutoff;j++){
            bestGuess+=split[j]+File.separator;
        }

        System.out.print("my guess: " + bestGuess);

        return Paths.get(bestGuess).toFile();
    }


    private static File turnJarOrApkIntoClassFileDir(File apkFile) {

        File jarFile = apkFile;
        if(apkFile.getName().contains(".apk")){
            //this is an apk file, first convert it into a jar file
            //command
            String outputFilePath=apkFile.getAbsolutePath().replace(".apk",".jar");

            //dex to jar sh is in AndroidTA_FaultLocalization/resources/delta_debugger/dex-tools-2.1
            // ./d2j-dex2jar.sh -f  "path to apk" -o "outputfile.jar"
            String command = System.getenv().get("ANDROID_FAULTLOCALIZATION_HOME")+"/resources/delta_debugger/dex-tools-2.1/d2j-dex2jar.sh -f "+apkFile.getAbsolutePath()+" -o "+outputFilePath;
            CommandThread dex2jarCommand = new CommandThread(command);
            dex2jarCommand.start();
            try {
                dex2jarCommand.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            jarFile= Paths.get(outputFilePath).toFile();
        }

        jarFile.renameTo(new File(jarFile.getAbsolutePath().replace(".jar", ".zip")));
        File zipFile = new File(jarFile.getAbsolutePath().replace(".jar", ".zip"));
        String destUnzipFile = zipFile.getAbsolutePath().replace(".zip","");

        try{
            ZipFile src = new ZipFile(zipFile);
            src.extractAll(destUnzipFile);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return Paths.get(destUnzipFile).toFile();
    }
}
