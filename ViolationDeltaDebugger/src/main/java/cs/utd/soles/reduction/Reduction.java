package cs.utd.soles.reduction;

import com.github.javaparser.ast.CompilationUnit;
import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;

public interface Reduction {

    void reduce(ArrayList<Object> requireds);

    boolean testBuild();
    boolean testViolation();

    boolean testChange(ArrayList<Pair<File, CompilationUnit>> newCuList,int unitP,CompilationUnit cu);
}
