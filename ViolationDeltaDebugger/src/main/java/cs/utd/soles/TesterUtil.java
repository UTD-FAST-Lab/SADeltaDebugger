package cs.utd.soles;


import java.io.*;


/**
 * This is used to test the AST against the target, it also parses the target file so it can be compared
 * */

//TODO:: APACHE STOPWATCH implement

public class TesterUtil /*implements ThreadHandler*/ {}

   /* public void runCCGCreator(String projectApkPath, String thisRunName){
        String[] command = {"java","-jar","/home/dakota/documents/AndroidTA_FaultLocalization/resources/modified_flowdroid/FlowDroid/soot-infoflow-cmd/target/soot-infoflow-cmd-jar-with-dependencies.jar"
            ,"-a",projectApkPath,"-p","/home/dakota/documents/Android/platforms/","-s","/home/dakota/documents/AndroidTA_FaultLocalization/resources/modified_flowdroid/FlowDroid/soot-infoflow-android/SourcesAndSinks.txt"};
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
            ProcessThread pThread = new ProcessThread(p,this,ProcessType.CALLGRAPH, 30000,-1);
            pThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCCGProcess(String projectAPKPath, String thisRunName){
        runCCGCreator(projectAPKPath,thisRunName);
    }

    boolean threadResult=false;
*/
   /* @Override
    public void handleThread(Thread thred, ProcessType t, String finalString, String finalString2) {

        //This new framework for handling threads should allow us to read Process output more elegantly
/*
        switch(t){
            //create_apk_process is just one gradlew assembleDebug


            case CALLGRAPH:
                synchronized (lockObj){
                    threadResult=handleCallGraph(finalString);
                    lockObj.notify();
                }
                break;
        }*/
/*
    //dependency graph is like, static so this can just access it and change its data up accordingly
    /*public boolean handleCallGraph(String contents){
        //Okay so we have the exact string we want we just need to match it up to what we already have,
        //ClassNode and DependencyGraph
        String[] lines = contents.split("\n+");
        for(String x: lines){
            //much like DependencyGraph.parseGraphFromDot()
            String[] leftRight = x.split(" -> ");

            System.out.println(x);
            //one problem, there might be "Ghost methods"
            //methods that flowdroid created but aren't actually real, so before we add any particular line to DependencyGraph.methodGraph we need to make sure it is a real thing
            //so basically, we gonna have to do some magic
            //furthermore, i am ignoring anonymous class methods for this, they are too annoying to implement for now
            //also we want only internal methods, both in and out, so resolve both before making an edge


            //okay to make sense of this, first we cut up the left side and find its AST and the node we think matches the signature, then we make a MethodNode
            //then we do the same for the right side
            //then we try to add to the graph

            //Okay lets find out the class for left side first, helps us get the right ast and verify this method exists
            String origin = leftRight[0];
            //cut up contents
            String[] originContents = convertNodeString(origin);
            //get the ast
            ClassNode parent = Runner.dg.getClassNodeForFilePath(Runner.getFilePathForClass(originContents[0]));
            if(null==parent){
                //this thing isn't in on our asts, just go on to the next
                System.out.println("Node parent: not found");
                continue;
            }
            System.out.println("Node parent: found");
            Node astNode = findASTNodeFromSignature(originContents, parent);
            if(astNode==null) {
                //this method wasn't found in the ast, just go on to the next
                System.out.println("ASTNode Node: Not Found");
                continue;
            }
            System.out.println("ASTNode Node: Found");
            String[] paramsCut= new String[originContents.length-3];
            for(int i=3;i<originContents.length;i++){
                paramsCut[i-3]=originContents[i].substring(originContents[i].lastIndexOf(".")+1);
            }
            MethodNode node = new MethodNode(originContents[2],originContents[1],paramsCut, parent, astNode);
            System.out.println("Node: " + node);



            //dependency
            String dependency = leftRight[1];
            String[] dependencyContents = convertNodeString(dependency);
            //System.out.println("Dependency contents: "+ Arrays.toString(dependencyContents));
            ClassNode dParent = Runner.dg.getClassNodeForFilePath(Runner.getFilePathForClass(dependencyContents[0]));
            if(null==dParent){
                //this thing isn't in on our asts, just go on to the next
                System.out.println("Dependency parent: not found");
                continue;
            }
            System.out.println("Dependency parent: "+dParent.getName());
            Node astDNode = findASTNodeFromSignature(dependencyContents, dParent);
            if(astDNode==null) {
                System.out.println("ASTNode Dependency: Not Found");
                continue;
            }
            System.out.println("ASTNode Dependency: Found");

            String[] paramsDCut= new String[dependencyContents.length-3];

            System.out.println(Arrays.toString(dependencyContents));
            System.out.println(paramsDCut.length);
            for(int i=3;i<dependencyContents.length;i++){
                paramsDCut[i-3]=dependencyContents[i].substring(dependencyContents[i].lastIndexOf(".")+1);
            }
            System.out.println(Arrays.toString(paramsDCut));
            MethodNode dNode = new MethodNode(dependencyContents[2],dependencyContents[1],paramsDCut, dParent, astDNode);
            System.out.println("Dependency: " + dNode);

            //add to callgraph
            Runner.dg.makeCallgraphEdge(node,dNode);

        }

        for(MethodNode g: Runner.dg.methodGraph){
            System.out.println(g);
        }

        return true;
    }

    public String[] convertNodeString(String nodeString){
        //'<' .* '>': <returnType> <methodName>(<>* parameterTypes)
        ArrayList<String> stringList = new ArrayList<>();
        //System.out.println("start nodeString: "+nodeString);
        //we are done with package/classname when we hit a :
        /*String packageClassName = nodeString.substring(1,nodeString.indexOf(": "));
        nodeString = nodeString.substring(0,packageClassName.length()+2);
        System.out.println("Changed nodeString: "+nodeString);

        //cut first and last
        nodeString = nodeString.substring(1,nodeString.length()-1).replace(": "," ");
        String[] elements = nodeString.split(" ");
        String classPackageName = elements[0];
        //System.out.println("ClassPackageName: "+classPackageName);
        String returnType = elements[1];
        //System.out.println("return type: "+returnType);
        String methodName = elements[2].substring(0,elements[2].indexOf("("));
        //System.out.println("method name: "+methodName);
        String[] parameterTypeStrings=null;

        int startParam=elements[2].indexOf("(");
        int endParam=elements[2].lastIndexOf(")");

        if(endParam==startParam+1){
            parameterTypeStrings=new String[0];
        }
        else if(elements[2].contains(",")) {
             parameterTypeStrings= elements[2].substring(startParam + 1, endParam).trim().split(",");
        }else{
            parameterTypeStrings=new String[1];
            parameterTypeStrings[0]=elements[2].substring(startParam+1,endParam);
        }

        //System.out.println("parameter types: "+Arrays.toString(parameterTypeStrings));
        String[] returnList = new String[3+parameterTypeStrings.length];
        returnList[0]=classPackageName;
        returnList[1]=returnType;
        returnList[2]=methodName;
        for(int i=3;i<returnList.length;i++){
            returnList[i]=parameterTypeStrings[i-3];
        }

        return returnList;
    }

    //if this returns null, then the method aint to be found in one our asts
    public Node findASTNodeFromSignature(String[] methodSig, ClassNode parent){
        //method sig is,
        //0 class/package name
        //1 return type
        //2 method name
        //N parameter Types


        //System.out.println("it finds a class: " + Arrays.toString(methodSig) + " IS IN "+parent.getName());
        //get the compilation unit we think its in
        CompilationUnit ourUnit = Runner.getASTForFile(parent.getFilePath());

        //System.out.println(ourUnit + "\n IT FINDS A UNIT for " + Arrays.toString(methodSig));
        Node[] foundNodeArr=new Node[1];
        traverseGraphAndFind(ourUnit,methodSig,foundNodeArr);
        Node foundNode = foundNodeArr[0];
        //System.out.println("Out the thing: "+foundNode);

        return foundNode;
    }

    //returns the node that this methodSignature is referring to.
    private void traverseGraphAndFind(Node cur, String[] methodSig, Node[] foundNode){
        if(matchesSig(cur, methodSig)){
            foundNode[0]=cur;
            //System.out.println("In the thing: "+foundNode[0]);
        }
        else{
            for(Node child:cur.getChildNodes())
                traverseGraphAndFind(child,methodSig,foundNode);
        }
    }

    //go through all asts and add each method to callgraph, we will add dependencies later
    private void makeCallGraphNodes(){
        for(Pair<File,CompilationUnit> pair: Runner.bestCUList){
            CompilationUnit rootNode = pair.getValue1();


        }

    }


    //interesting little problem here, callgraph isn't exactly what we want either,
    //if I need to reconstruct this AST and the goal is no uncompilable code, I need to know what my little methods might need to compile
    //class A{
    // method a1();
    // method a2();
    // }
    //class B{
    // main(){
    //  a1();
    //  }
    // }
    //this is going to get interesting
    private void traverseAndAddMethods(Node cur){
        if(cur instanceof MethodDeclaration){
            //this is a method handle according
        }
        for(Node child:cur.getChildNodes()){
            if(child instanceof ClassOrInterfaceDeclaration){
                //this is a class and check to make sure it isn't inner, we will handle those in a bit
                if(((ClassOrInterfaceDeclaration)child).isInnerClass()){
                    return;
                }

            }
            else if(child instanceof ObjectCreationExpr){
                //this is an object creation expr, make sure its not an anonymous class we don't want to add their methods to callgraph just yet.
                if(((ObjectCreationExpr)child).getAnonymousClassBody().isPresent()){
                    return;
                }
            }
            traverseAndAddMethods(child);
        }
    }


    //does this thing match the signature?
    private boolean matchesSig(Node cur, String[] methodSig) {
        if(!(cur instanceof MethodDeclaration))
            return false;

        MethodDeclaration node = (MethodDeclaration) cur;
        //method sig is,
        //0 class/package name
        //1 return type
        //2 method name
        //N parameter Types
        System.out.println("Method sig: "+Arrays.toString(methodSig));
        //funny how java works, im pretty sure name and parameter types are all we need to identify a method, the method type can't be different and the only
        //thing that can change is if it is overloaded

        String methodNameAst = node.getNameAsString();
        //System.out.println(methodNameAst);
        if(!methodNameAst.equals(methodSig[2])){
            //names dont match
            System.out.println("Name dont matches!");
            return false;
        }
        System.out.println("Name matches!");

        String[] paramsCut= new String[methodSig.length-3];
        for(int i=3;i<methodSig.length;i++){
            paramsCut[i-3]=methodSig[i].substring(methodSig[i].lastIndexOf(".")+1);
        }
        System.out.println("Param cut: "+ Arrays.toString(paramsCut));

        //idk how to anything about this;
        NodeList<Parameter> parameters = node.getParameters();
        if(parameters.size()!=paramsCut.length) {
            System.out.println("Size no match paramsSize: "+ parameters.size()+"supposed: "+paramsCut.length);
            return false;
        }
        System.out.println("Size match");
        int i=0;
        for(Parameter x: parameters){
            //OKAY SO IM LIKE 100% SURE THIS IS BUGGY, its not the full name so in the awful case where you have two classes named the same, that are both parameters,
            //while also having the same classname, this will give you the wrong node
            //while also having same method name
            //while also having exact same parameter length and appearing in same position
            if(!x.getTypeAsString().equals(paramsCut[i])){
                return false;
            }
            i++;
        }

        // if we reach here, then I guess this node is equal.
        return true;
    }
     */

