package cs.utd.soles.determinism;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import cs.utd.soles.setup.SetupClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CheckDeterminism {

    public static boolean checkOrCreate(SetupClass programinfo, Node changedNode, List<Node> removedNodes, String changeNum){

        String uniqueNameString = programinfo.getThisRunName();

        if(programinfo.getArguments().getValueOfArg("NO_OPTIMIZATION").isPresent()){
            uniqueNameString=uniqueNameString+"_noopt";
        }
        if(programinfo.getArguments().getValueOfArg("NO_ABSTRACT_METHODS").isPresent()){
            uniqueNameString=uniqueNameString+"_nam";
        }
        if(programinfo.getArguments().getValueOfArg("CLASS_REDUCTION").isPresent()){
            uniqueNameString=uniqueNameString+"_binary";
        }

        File dirfile = new File("debugger/masterchange/");
        dirfile.mkdirs();
        //either check or create
        String fp = "debugger/masterchange/"+uniqueNameString+"_"+changeNum+".java";
        File f = new File(fp);
        return create(f,changedNode,removedNodes);

    }

    private static boolean create(File f, Node changeNode, List<Node> listNodes){
        if(f.exists()){
            return true;
        }
        FileWriter fw = null;
        try {
            f.createNewFile();
            fw = new FileWriter(f);
            fw.write("Parent: "+changeNode.toString()+"\n");
            fw.write("[\n");
            for(Node x : listNodes){
                fw.write(x+"\n,\n");
            }
            fw.write("\n]");

            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
