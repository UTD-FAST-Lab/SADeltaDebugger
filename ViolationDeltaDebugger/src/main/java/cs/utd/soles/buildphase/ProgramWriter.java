package cs.utd.soles.buildphase;

import com.github.javaparser.ast.CompilationUnit;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ProgramWriter {

    public static void cleanseFiles(ArrayList<Pair<File, CompilationUnit>> originalUnits) {
        for(Pair<File,CompilationUnit> p : originalUnits){
            File path = p.getValue0();
            if(path.exists())
                path.delete();
        }
    }

    public static void saveCompilationUnits(ArrayList<Pair<File, CompilationUnit>> compilationUnits, int positionChanged, CompilationUnit changedUnit) throws IOException {
        int i=0;
        for(Pair<File,CompilationUnit> x: compilationUnits){

            FileWriter fw = new FileWriter(x.getValue0());

            if(i==positionChanged){
                fw.write(changedUnit.toString());
            }else {
                fw.write(x.getValue1().toString());
            }
            fw.flush();
            fw.close();
            i++;
        }
    }
}
