package cs.utd.soles.reduction;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarInputStream;

// import org.eclipse.core.internal.resources.Project;
// import org.eclipse.jdt.internal.core.JavaProject;
import org.javatuples.Pair;

import com.github.javaparser.ast.CompilationUnit;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.util.SanityException;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject.JavaProjectBuilder;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

public class MethodReduction implements Reduction {

    sootup.java.core.JavaProject project = null;

    long timeoutTime;
    SetupClass programInfo;

    public MethodReduction(SetupClass programInfo, /* , ArgsHandler ar, */ long timeoutTime) {
        this.programInfo = programInfo;
        this.timeoutTime = timeoutTime + System.currentTimeMillis();

        this.createCG();
    }

    public void reduce(ArrayList<Object> requireds) throws SanityException {

    }

    public int testBuild() {
        return 0;
    }

    public int testViolation() {
        return 0;
    }

    public boolean testChange(ArrayList<Pair<File, CompilationUnit>> newCuList, int unitP, CompilationUnit cu)
            throws SanityException {
        return false;
    }

    public String findEntryPoint() {
        // Jar files helpfully contain their own entrypoint
        String entryPoint = "";
        try {

            JarInputStream jarStream = new JarInputStream(new FileInputStream(programInfo.getArguments().target));
            entryPoint = jarStream.getManifest().getMainAttributes().getValue("Main-Class");
            jarStream.close();

        } catch (Exception e) { // There shouldnt be any file errors since the files have already been read
                                // earlier
            System.out.println(e.toString());
        }
        return entryPoint;

    }


    private class Node{
        //represent a method in both call graph and ast

        public List<Pair<Object, Object>>  calls; //Incoming AST calls to the method, and their parents
        public Object cgNode; //the method in the cg
        public Object astNode; //the method in the ast
        
        
        Node(){};
    }


    public void createCG() {
        // Initialize and create the call graph using sootup


        JavaLanguage language = new JavaLanguage(11);
        JavaProjectBuilder builder = new JavaProjectBuilder(language);

        // Add source locations to the sootup project
        for (File file : programInfo.getArguments().sources) {
            if(file.getAbsolutePath() != null){
                //builder.addInputLocation(new JavaSourcePathAnalysisInputLocation(file.getAbsolutePath()));
                builder.addInputLocation(new PathBasedAnalysisInputLocation (file.toPath(), SourceType.Application));
            }
        }
        
        builder.addInputLocation(
            new JavaClassPathAnalysisInputLocation(
                System.getProperty("java.home") + "/lib/jrt-fs.jar"));
        project = builder.build();

        // Create the view
        //JavaView view = (JavaView) project.createFullView();
        JavaView view = project.createView();
        String entryPoint = findEntryPoint();
        System.out.println(entryPoint);
        ClassType classType = project.getIdentifierFactory().getClassType(entryPoint);

        MethodSignature entryMethodSignature = JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                classType,
                "main",
                "void",
                Collections.singletonList("java.lang.String[]")
            );
        view.getMethod(entryMethodSignature);

        //CallGraphAlgorithm cga_builder = new ClassHierarchyAnalysisAlgorithm(view, view.getTypeHierarchy());
        CallGraphAlgorithm cga_builder = new RapidTypeAnalysisAlgorithm(view);
        CallGraph cg = cga_builder.initialize(Collections.singletonList(entryMethodSignature));
        
    }

}
