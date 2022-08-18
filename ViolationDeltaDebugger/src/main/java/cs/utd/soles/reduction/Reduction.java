package cs.utd.soles.reduction;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.util.SanityException;
import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;

public interface Reduction {

    void reduce(ArrayList<Object> requireds) throws SanityException;

    int testBuild();
    int testViolation();

    boolean testChange(ArrayList<Pair<File, CompilationUnit>> newCuList,int unitP,CompilationUnit cu) throws SanityException;
}
