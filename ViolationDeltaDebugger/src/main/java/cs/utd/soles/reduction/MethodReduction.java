package cs.utd.soles.reduction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarInputStream;


import org.javatuples.Pair;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import cs.utd.soles.ScriptRunner;
import cs.utd.soles.buildphase.ProgramWriter;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.util.SanityException;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaProject.JavaProjectBuilder;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class MethodReduction implements Reduction {

    CallGraph cg = null;
    MethodSignature cgRoot = null;
    
    ArrayList<Pair<File,CompilationUnit>> finalCuList;
    ArrayList<Pair<File,CompilationUnit>> newCuList;

    long timeoutTime;
    SetupClass programInfo;

    public MethodReduction(SetupClass programInfo, /* , ArgsHandler ar, */ long timeoutTime) {
        this.programInfo = programInfo;
        this.timeoutTime = timeoutTime + System.currentTimeMillis();


    }

    public void reduce(ArrayList<Object> requireds) throws SanityException {
        ArrayList<Pair<File,CompilationUnit>> bestCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        newCuList = new ArrayList<>();
        for(Pair<File, CompilationUnit> cu : bestCuList){
            newCuList.add(new Pair<File, CompilationUnit>(cu.getValue0(), cu.getValue1().clone()));
        }

        this.createCG();
        Set<MethodSignature> visited = new HashSet<MethodSignature>();
        List<MethodSignature> current = new ArrayList<MethodSignature>();
        for(MethodSignature method : cg.callsFrom(cgRoot)){
            if(!visited.contains(method) && !method.getName().equals("<init>")){
                visited.add(method);
                current.add(method);
            }
        }
        List<MethodSignature> fullyRemoved = new ArrayList<>();
        callGraphReduction(newCuList, visited, current, fullyRemoved);
        
        // get final cu list into main program by cannabalizing bestCuList's reference
        bestCuList.clear();
        for(Pair<File, CompilationUnit> cu : finalCuList){
            bestCuList.add(cu);
        }

    }

    private void callGraphReduction(ArrayList<Pair<File, CompilationUnit>> cuList, Set<MethodSignature> visited, List<MethodSignature> current, List<MethodSignature> fullyRemoved){       
        for(int n=1; n <= current.size(); n = (n*2 <= current.size()) ? n*2 : current.size()){
            List<List<MethodSignature>> chunks = new ArrayList<>();
            List<MethodSignature> newChunk = null;
            int chunkSize = current.size() / n;
            for(int i = 0; i < current.size(); i++){
                if(i % chunkSize == 0){
                    if(newChunk != null){
                        chunks.add(newChunk);
                    }
                    newChunk = new ArrayList<>();
                }
                newChunk.add(current.get(i));
            }
            // Fill in last part of the chunks list
            if(newChunk.size() != 0){chunks.add(newChunk);}

            //try removing each chunk
            for(List<MethodSignature> chunk : chunks){
                Pair<Boolean, ArrayList<Pair<File, CompilationUnit>>> result = tryRemoval(cuList, chunk, fullyRemoved);
                if(result.getValue0()){ //If failure is preserved
                    //update cuList to new trimmed cuList
                    cuList = result.getValue1();
                    //add chunk to fully removed list
                    for(MethodSignature method : chunk){
                        fullyRemoved.add(method);
                    }
                }  
                for(Pair<File, CompilationUnit> cu : cuList){
                    System.out.println(cu);
                } 
            }
            if(n == current.size()){
                break; //explicit break here after last run to avoid making the for loop into a programming crime
            }
        }
        //Do next layer
        List<MethodSignature> nextCurrent = new ArrayList<>();
        for(MethodSignature nextMethod : current){
            if (!fullyRemoved.contains(nextMethod)){
                for(MethodSignature child : cg.callsFrom(nextMethod)){
                    if(!visited.contains(child)){
                        visited.add(child);
                        nextCurrent.add(child);
                    }
                }
            }
        }
        finalCuList = cuList;
        if(nextCurrent.size() != 0 && System.currentTimeMillis() < timeoutTime){
            callGraphReduction(cuList, visited, nextCurrent, fullyRemoved);
        }
        else{
            return;
        }
    }


    // Returns true if the changes should be made
    private Pair<Boolean, ArrayList<Pair<File, CompilationUnit>>> tryRemoval(ArrayList<Pair<File, CompilationUnit>> cuList, List<MethodSignature> toRemove, List<MethodSignature> alreadyRemoved){
        
        //clone cuList
        ArrayList<Pair<File, CompilationUnit>> newCuList = new ArrayList<>();
        for(Pair<File, CompilationUnit> cu : cuList){
            newCuList.add(new Pair<File,CompilationUnit>(cu.getValue0(), cu.getValue1().clone()));
        }

        // Write back current source to not confuse symbol solver
        try{
            ProgramWriter.saveCompilationUnits(newCuList,-1,null);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //Generate cuList specific information
        //It has to run many times but there isn't much of a way around that
        Map<MethodSignature, MethodDeclaration> cgToAST = matchCGtoAST(newCuList);
        List<MethodCallExpr> methodCallExprs = findAllASTMethodCalls(newCuList);

        //remove the methods from the chunk
        for(MethodSignature method : toRemove){
            MethodDeclaration methodDeclaration = cgToAST.get(method);
            if(methodDeclaration != null){
                removeASTMethodCall(methodDeclaration, methodCallExprs);
            }
        }
       
        //Find orphan methods
        Set<MethodSignature> allRemovedMethods = new HashSet<>(alreadyRemoved);
        allRemovedMethods.addAll(toRemove);
        List<MethodSignature> orphans = findOrphans(allRemovedMethods);
        //Remove the orphan methods
        for(MethodSignature method : orphans){
            MethodDeclaration methodDeclaration = cgToAST.get(method);
            if(methodDeclaration != null){
                removeASTMethodCall(methodDeclaration, methodCallExprs);
            }
        }
        

        Pair<Boolean, ArrayList<Pair<File, CompilationUnit>>> returnValue = null;
        if(testChange(newCuList, -1, null)){
            returnValue = new Pair<>(true, newCuList);
        } else{
            returnValue = new Pair<>(false, cuList);
        }
        return returnValue;
    }


    private void removeASTMethodCall(MethodDeclaration method, List<MethodCallExpr> methodCalls){
        try{
            ResolvedMethodDeclaration resolvedMethod = method.resolve();
            for(MethodCallExpr call : methodCalls){
                try{
                    ResolvedMethodDeclaration callMethod = call.resolve(); //beware
                    if(Objects.equals(callMethod.getQualifiedSignature(), resolvedMethod.getQualifiedSignature())){
                        System.out.println("####\n" + resolvedMethod + "\n" + call + "\n" + callMethod + "####");
                        // If assignment, remove the assignment part but not the declaration part
                        // For example, int i = methodcall(); turns into int i;
                        // Java will initialize with whatever default null equivelent value is right for the type
                        if(call.getParentNode().get() instanceof VariableDeclarator){
                            VariableDeclarator declarator = (VariableDeclarator) call.getParentNode().get();
                            declarator.removeInitializer();
                            // null will get implicitly cast to whatever type
                            declarator.setInitializer(new NullLiteralExpr());
                        }
                        // if(call.getParentNode().get() instanceof ExpressionStmt){
                        //     call.remove();
                        // }
                        else{
                            //sometimes the call is a required property of the parent and so cannot be removed
                            call.removeForced();
                            // Node remove = call;
                            // while(!remove.remove()){
                            //     remove = remove.getParentNode().get();
                            // }
                        }
    
                    }
                }
                catch(UnsolvedSymbolException e){;}//This typically means that the call expression refers to an already remved            
            }
        }
        catch(IllegalStateException e){
            System.out.println(e);
        }
        catch(UnsolvedSymbolException e){
            System.out.println("\033[31m" + e + "\033[0m]"); //make it red because its not a good exception to have
        }
        method.remove();
    }

    //TODO discuss if this should be implemented
    private void removeASTObjectCreation(MethodDeclaration method, List<ObjectCreationExpr> calls){
        // ResolvedMethodDeclaration resolvedMethod = method.resolve();
        // for(ObjectCreationExpr call : calls){
        //     ResolvedConstructorDeclaration callMethod = call.resolve();
        //     if(Objects.equals(callMethod.getQualifiedSignature(), resolvedMethod.getQualifiedSignature())){ //TODO investigate matching code
        //         System.out.println(resolvedMethod + "\n" + call + "\n" + callMethod);
        //         // If assignment, remove the assignment part but not the declaration part
        //         // For example, int i = methodcall(); turns into int i;
        //         // Java will initialize with whatever default null equivelent value is right for the type
        //         if(call.getParentNode().get() instanceof VariableDeclarator){ //TODO fix to allow nesting
        //             ( (VariableDeclarator) call.getParentNode().get()).removeInitializer();
        //         }
        //         call.remove(); //replace with something

        //     }
        // }
        method.remove();
    }


    private List<MethodCallExpr> findAllASTMethodCalls(ArrayList<Pair<File, CompilationUnit>> cuList){
        // Assemble AST methods into a nice list 
        List<MethodCallExpr> methodCalls = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        for(Pair<File,CompilationUnit> cu : cuList){
            queue.add(cu.getValue1());
        }
        while(!queue.isEmpty()){
            Node cur = queue.remove();

            if( ! (cur instanceof MethodCallExpr)){
                for(Node children : cur.getChildNodes()){
                    queue.add(children);
                }
            } else{
                methodCalls.add((MethodCallExpr) cur);
            }
        }
        return methodCalls;
    }


    private List<MethodSignature> findOrphans(Set<MethodSignature> removedMethods){
        List<MethodSignature> orphans = new ArrayList<>();

        //get list of reachable methods with graph traversal
        List<MethodSignature> reachable = new ArrayList<>();
        reachable.add(cgRoot);
        Queue<MethodSignature> queue = new LinkedList<>();
        for(MethodSignature method :  cg.callsFrom(cgRoot)){
            if(!removedMethods.contains(method)){
                queue.add(method);
                reachable.add(method);
            }
        }
        while(queue.size() != 0){
            MethodSignature cur = queue.poll();
            for(MethodSignature method : cg.callsFrom(cur))
            if(!removedMethods.contains(method)){
                queue.add(method);
                reachable.add(method);
            }
        }

        // If method is not reachable, its an orphan
        for(MethodSignature method : cg.getMethodSignatures()){
            if(!reachable.contains(method) && !removedMethods.contains(method)){
                orphans.add(method);
            }
        }

        return orphans;
    }


    @Override
    public int testBuild() {
        try {
            return ScriptRunner.runBuildScript(programInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } return -1;
    }

    @Override
    public int testViolation() {
        try {
            return ScriptRunner.runTestScript(programInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } return -1;
    }

    @Override
    public boolean testChange(ArrayList<Pair<File, CompilationUnit>> newCuList, int cupos, CompilationUnit cu) {
        try {
            ProgramWriter.saveCompilationUnits(newCuList,cupos,null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        //always the same after writing
        //logging stuff needs to be made
        programInfo.getPerfTracker().startTimer("compile_timer");
        if(testBuild() != 0) {
            programInfo.getPerfTracker().stopTimer("compile_timer");
            programInfo.getPerfTracker().addTime("time_bad_compile_runs_binary",
                    programInfo.getPerfTracker().getTimeForTimer("compile_timer"));
            programInfo.getPerfTracker().resetTimer("compile_timer");
            programInfo.getPerfTracker().addCount("bad_compile_runs_binary",1);

            return false;
        }
        programInfo.getPerfTracker().addCount("good_compile_runs_binary",1);
        programInfo.getPerfTracker().stopTimer("compile_timer");
        programInfo.getPerfTracker().addTime("time_good_compile_runs_binary",
                programInfo.getPerfTracker().getTimeForTimer("compile_timer"));
        programInfo.getPerfTracker().resetTimer("compile_timer");

        programInfo.getPerfTracker().startTimer("recreate_timer");
        if(testViolation() != 0) {
            programInfo.getPerfTracker().addCount("bad_recreate_runs_binary", 1);
            programInfo.getPerfTracker().stopTimer("recreate_timer");
            programInfo.getPerfTracker().addTime("time_bad_recreate_runs_binary",
                    programInfo.getPerfTracker().getTimeForTimer("recreate_timer"));
            programInfo.getPerfTracker().resetTimer("recreate_timer");
            return false;
        }
        programInfo.getPerfTracker().stopTimer("recreate_timer");
        programInfo.getPerfTracker().addTime("time_good_recreate_runs_binary",
                programInfo.getPerfTracker().getTimeForTimer("recreate_timer"));
        programInfo.getPerfTracker().resetTimer("recreate_timer");
        programInfo.getPerfTracker().addCount("good_recreate_runs_binary",1);
        return true;
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
                //maybe change to JavaClassPathAnalysisInputLocation with a jar file, should work about the same
                //inputLoc = new JavaSourcePathAnalysisInputLocation(SourceType.Application, file.getAbsolutePath());
                inputLoc = new JavaClassPathAnalysisInputLocation(programInfo.getArguments().target.getAbsolutePath());
                
                builder.addInputLocation(inputLoc);
            }
        }

        builder.addInputLocation(
            new JavaClassPathAnalysisInputLocation(
                System.getProperty("java.home") + "/lib/jrt-fs.jar")); //TODO make less fragile, currently depends on exact runtime version
        JavaProject project = builder.build();
        
        // Create the view
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
        cg = cga_builder.initialize(Collections.singletonList(entryMethodSignature));
        cgRoot = entryMethodSignature;
        System.out.println("\n\n####################\n\n" + cg + "\n\n####################\n");
    }

    
    private Map<MethodSignature, MethodDeclaration> matchCGtoAST(ArrayList<Pair<File,CompilationUnit>> cuList){
        Map<MethodSignature, MethodDeclaration> cgToAST = new HashMap<>();

        // Assemble AST methods into a nice list 
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        for(Pair<File,CompilationUnit> cu : cuList){
            queue.add(cu.getValue1());
        }
        while(!queue.isEmpty()){
            Node cur = queue.remove();

            if((cur instanceof MethodDeclaration)){
                methodDeclarations.add((MethodDeclaration) cur);
            }
            for(Node children : cur.getChildNodes()){
                queue.add(children);
            }
        
        }


        // sootup puts in $ when javaparser puts a . sometimes
        // custom comparison function to fix this
        Function<Pair<String,String>, Boolean> compareClassNames = (Pair<String, String> pair) -> {
            String a = pair.getValue0();
            String b = pair.getValue1();
            if(a.length() != b.length()){ return false;}
            for(int i = 0; i < a.length(); i++){
                if(
                    !(a.charAt(i) == b.charAt(i)
                    ||
                    a.charAt(i) == '$' && b.charAt(i) == '.'
                    ||
                    a.charAt(i) == '.' && b.charAt(i) == '$'
                    )
                ){return false;}
            }
            return true;
        };

        //need mapping from cg signature to AST method construct
        for(MethodSignature signature : cg.getMethodSignatures()){
            for(MethodDeclaration declaration : methodDeclarations){
                // Check if they represent the same method, by checking class/package name, method name, and parameter types
                if(
                    Objects.equals(declaration.getNameAsString(), signature.getName()) &&
                    compareClassNames.apply(new Pair<String, String>(
                        ( (ClassOrInterfaceDeclaration) declaration.getParentNode().get()).getFullyQualifiedName().get(),
                        signature.getDeclClassType().getFullyQualifiedName()
                    ))
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
        return cgToAST;

    }

}
