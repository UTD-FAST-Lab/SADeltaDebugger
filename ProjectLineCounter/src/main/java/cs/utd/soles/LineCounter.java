package cs.utd.soles;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineCounter {

    /*
     * given a path the src code of a java project, count total lines of code
     *
     * */

    public static long countLinesDir(String srcDirPath) throws FileNotFoundException {

        File f = Paths.get(srcDirPath).toFile();

        if(!f.exists()){
            throw new FileNotFoundException(srcDirPath + "not found");
        }

        String[] extensions = {"java"};
        List<File> allJFiles = ((List<File>) FileUtils.listFiles(f, extensions, true));

        long totalCount=0;
        for(File x: allJFiles){
            totalCount+=countLinesForFile(x);
        }
        return totalCount;

    }
    //count lines for specific file
    private static int countLinesForFile(File javaFile) throws FileNotFoundException {
        int lines=0;
        Scanner sc = new Scanner(javaFile);
        String bigString="";
        while(sc.hasNextLine()){
            String line = sc.nextLine()+"\n";
            bigString+=line;

        }

        //get rid of all the comments
        //this one causes stack overflow for whatever reason
        //Pattern blockP = Pattern.compile("/\\*(.|[\\r\\n])*?\\*/");
        Pattern blockP = Pattern.compile("/\\*(?s).*?\\*/");
        Pattern lineP = Pattern.compile("//.*");

        bigString = bigString.replaceAll(lineP.pattern(),"");
        bigString = bigString.replaceAll(blockP.pattern(),"");


        //bigString = bigString.replaceAll(blockP.pattern(),"");


        //get rid of all the blank space
        String[] bigStringArr = bigString.split("\n");
        for(String x: bigStringArr){
            String trimmedLine=x.trim();
            if(!trimmedLine.isEmpty()){
                lines++;
            }

        }
        return lines;
    }




}
