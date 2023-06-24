package cs.utd.soles.reduction;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.jar.JarInputStream;

// import org.eclipse.core.internal.resources.Project;
// import org.eclipse.jdt.internal.core.JavaProject;
import org.javatuples.Pair;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.resolution.types.ResolvedType;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.util.SanityException;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootClassSource;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaProject.JavaProjectBuilder;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

public class MethodReduction implements Reduction {

    CallGraph cg = null;

    ArrayList<Pair<File,CompilationUnit>> oldCuList;
    ArrayList<Pair<File,CompilationUnit>> newCuList;

    long timeoutTime;
    SetupClass programInfo;

    public MethodReduction(SetupClass programInfo, /* , ArgsHandler ar, */ long timeoutTime) {
        this.programInfo = programInfo;
        this.timeoutTime = timeoutTime + System.currentTimeMillis();


    }

    public void reduce(ArrayList<Object> requireds) throws SanityException {
        ArrayList<Pair<File,CompilationUnit>> bestCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        oldCuList = bestCuList;
        newCuList = bestCuList;

        this.createCG();
        this.matchCGtoAST();
    
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

    public void createCG() {
        // Initialize and create the call graph using sootup


        JavaLanguage language = new JavaLanguage(11);
        JavaProjectBuilder builder = new JavaProjectBuilder(language);

        // Add source locations to the sootup project
        AnalysisInputLocation inputLoc = null;
        for (File file : programInfo.getArguments().sources) {
            if(file.getAbsolutePath() != null){
                inputLoc = new JavaSourcePathAnalysisInputLocation(SourceType.Application, file.getAbsolutePath());
                //maybe change to JavaClassPathAnalysisInputLocation with a jar file, should work about the same
                
                builder.addInputLocation(inputLoc);
            }
        }


        
        builder.addInputLocation(
            new JavaClassPathAnalysisInputLocation(
                System.getProperty("java.home") + "/lib/jrt-fs.jar"));
        JavaProject project = builder.build();
        

        // Create the view
        JavaView view = project.createView();

        String entryPoint = findEntryPoint();
        System.out.println(entryPoint);
        ClassType classType = project.getIdentifierFactory().getClassType(entryPoint);

        SootClass<JavaSootClassSource> sootClass = (SootClass<JavaSootClassSource>) view.getClass(classType).get();

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
        cg = cga_builder.initialize(Collections.singletonList(entryMethodSignature));
        System.out.println("\n\n####################\n\n" + cg + "\n\n####################\n\n");
    }


    //Method Declaration is null if method has already been removed from the ast in an earlier pass
    Map<MethodSignature, MethodDeclaration> cgToAST = new HashMap<>();
    private void matchCGtoAST(){
     

        // Assemble AST methods into a nice list 
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        for(Pair<File,CompilationUnit> cu : newCuList){
            queue.add(cu.getValue1());
        }
        while(!queue.isEmpty()){
            Node cur = queue.remove();

            if( ! (cur instanceof MethodDeclaration)){
                for(Node children : cur.getChildNodes()){
                    queue.add(children);
                }
            } else{
                methodDeclarations.add((MethodDeclaration) cur);
            }
        }

        //need mapping from cg signature to AST method construct
        for(MethodSignature signature : cg.getMethodSignatures()){
            for(MethodDeclaration declaration : methodDeclarations){
                // Check if they represent the same method, by checking class/package name, method name, and parameter types
                if(
                    Objects.equals(declaration.getNameAsString(), signature.getName()) &&
                    Objects.equals(
                        ( (ClassOrInterfaceDeclaration) declaration.getParentNode().get()).getFullyQualifiedName().get(),
                        signature.getDeclClassType().getFullyQualifiedName()
                    )
                ){
                    Boolean allTypesMatch = true;
                    NodeList<Parameter> parameters = declaration.getParameters();
                    for(int i = 0; i<signature.getParameterTypes().size(); i++){
                        Type cgType = signature.getParameterTypes().get(i);
                        // resolving is required so that the ast type is fully qualified
                        ResolvedType astType = parameters.get(i).getType().resolve();
                        if(! Objects.equals(cgType.toString(), astType.describe())){
                            allTypesMatch = false;
                        }
                    }
                    if(allTypesMatch){
                        cgToAST.put(signature, declaration);
                    }
                }
            }
        }


    }

}
