package cs.utd.soles.reduction;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.utdallas.cs.alps.flows.Flow;
import com.utdallas.cs.alps.flows.Flowset;
import cs.utd.soles.buildphase.BuildScriptRunner;
import cs.utd.soles.buildphase.ProgramWriter;
import cs.utd.soles.determinism.CheckDeterminism;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.testphase.TestScriptRunner;
import org.javatuples.Pair;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HDDReduction implements Reduction{

    long timeoutTime;
    SetupClass programInfo;
    boolean checkDeterminism;
    public HDDReduction(SetupClass programInfo, long timeoutTime){
        this.programInfo=programInfo;
        this.timeoutTime=timeoutTime+System.currentTimeMillis();
        checkDeterminism = programInfo.getArguments().getValueOfArg("CHECK_DETERMINISM").isPresent()? (boolean)programInfo.getArguments().getValueOfArg("CHECK_DETERMINISM").get():false;
    }

    @Override
    public void reduce(ArrayList<Object> requireds) {
        ArrayList<Pair<File,CompilationUnit>> bestCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        hddReduction(bestCuList);
    }

    @Override
    public boolean testBuild() {
        return BuildScriptRunner.runBuildScript(programInfo);
    }

    @Override
    public boolean testViolation() {
        return TestScriptRunner.runTestScript(programInfo);
    }

    @Override
    public boolean testChange(ArrayList<Pair<File, CompilationUnit>> newCuList, int unitP, CompilationUnit cu) {

        try {
            ProgramWriter.saveCompilationUnits(newCuList,unitP,cu);
        } catch (IOException e) {
            e.printStackTrace();
        }

        programInfo.getPerfTracker().startTimer("compile_timer");
        if(!testBuild()) {
            programInfo.getPerfTracker().stopTimer("compile_timer");
            programInfo.getPerfTracker().addTime("time_bad_compile_runs_hdd",programInfo.getPerfTracker().getTimeForTimer("compile_timer"));
            programInfo.getPerfTracker().resetTimer("compile_timer");
            programInfo.getPerfTracker().addCount("bad_compile_runs_hdd",1);

            return false;
        }
        programInfo.getPerfTracker().addCount("good_compile_runs_hdd",1);
        programInfo.getPerfTracker().stopTimer("compile_timer");
        programInfo.getPerfTracker().addTime("time_good_compile_runs_hdd",programInfo.getPerfTracker().getTimeForTimer("compile_timer"));
        programInfo.getPerfTracker().resetTimer("compile_timer");

        programInfo.getPerfTracker().startTimer("recreate_timer");
        if(!testViolation()) {
            programInfo.getPerfTracker().addCount("bad_recreate_runs_hdd", 1);
            programInfo.getPerfTracker().stopTimer("recreate_timer");
            programInfo.getPerfTracker().addTime("time_bad_recreate_runs_hdd",
                    programInfo.getPerfTracker().getTimeForTimer("recreate_timer"));
            programInfo.getPerfTracker().resetTimer("recreate_timer");
            return false;
        }
        programInfo.getPerfTracker().stopTimer("recreate_timer");
        programInfo.getPerfTracker().addTime("time_good_recreate_runs_hdd",
                programInfo.getPerfTracker().getTimeForTimer("recreate_timer"));
        programInfo.getPerfTracker().resetTimer("recreate_timer");
        programInfo.getPerfTracker().addCount("good_recreate_runs_hdd",1);
        return true;
    }

    private boolean minimized=false;
    public void hddReduction(ArrayList<Pair<File, CompilationUnit>> bestCuList){

        minimized=false;
        programInfo.getPerfTracker().startTimer("hdd_timer");
        while(!minimized&&System.currentTimeMillis()<timeoutTime){
            minimized=true;
            programInfo.getPerfTracker().addCount("total_rotations",1);
            int i=0;
            for (Pair<File, CompilationUnit> compilationUnit : bestCuList) {
                //if we are under the time limit, traverse the tree
                if(System.currentTimeMillis()<timeoutTime)
                    traverseTree(i, compilationUnit.getValue1(), bestCuList);
                i++;
            }
        }
        programInfo.getPerfTracker().stopTimer("hdd_timer");
    }

    private void traverseTree(int currentCU, Node currentNode, ArrayList<Pair<File, CompilationUnit>> bestCuList){


        if(!currentNode.getParentNode().isPresent()&&!(currentNode instanceof CompilationUnit)||currentNode==null){
            return;
        }
        //no longer recur if we are past the time limit
        if(timeoutTime<System.currentTimeMillis())
            return;
        //process node
        process(currentCU, currentNode, bestCuList);
        //traverse children
        for(Node x: currentNode.getChildNodes()){

            traverseTree(currentCU, x, bestCuList);
        }

    }

    private void process(int currentCUPos, Node currentNode, ArrayList<Pair<File, CompilationUnit>> bestCuList){

        if(!currentNode.getParentNode().isPresent()&&!(currentNode instanceof CompilationUnit)){
            return;
        }
        if(currentNode instanceof ClassOrInterfaceDeclaration){
            ClassOrInterfaceDeclaration node = (ClassOrInterfaceDeclaration) currentNode;

            List<Node> childList = new ArrayList<Node>();
            for(Node x: node.getChildNodes()){
                if(x instanceof BodyDeclaration<?>){
                    childList.add(x);
                }
            }
            handleNodeList(currentCUPos,currentNode, childList, bestCuList);

        }
        if(currentNode instanceof BlockStmt) {

            BlockStmt node = ((BlockStmt) currentNode);
            List<Node> childList = new ArrayList<>();
            for(Node x: node.getChildNodes()){
                if(x instanceof Statement){
                    childList.add(x);
                }
            }
            handleNodeList(currentCUPos,currentNode, childList, bestCuList);
        }


    }

    private void handleNodeList(int compPosition, Node currentNode, List<Node> childList, ArrayList<Pair<File, CompilationUnit>> bestCuList){

        //make a copy of the tree
        CompilationUnit copiedUnit = bestCuList.get(compPosition).getValue1().clone();
        Node copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
        ArrayList<Node> alterableList = new ArrayList<Node>(childList);
        ArrayList<Node> copiedList = getCurrentNodeList(copiedNode, alterableList);



        //change the copy
        for(int i=copiedList.size();i>0;i/=2){
            for(int j=0;j<copiedList.size();j+=i){
                programInfo.getPerfTracker().addCount("ast_changes",1);
                //check timeout
                if(timeoutTime<System.currentTimeMillis())
                    return;

                List<Node> subList = new ArrayList<>(copiedList.subList(j,Math.min((j+i),copiedList.size())));
                List<Node> removedNodes = new ArrayList<>();
                List<Node> alterableRemoves = new ArrayList<>();
                int index=j;
                for(Node x: subList){
                    if(copiedList.contains(x)){
                        copiedNode.remove(x);
                        removedNodes.add(x);
                        alterableRemoves.add(alterableList.get(index));

                    }
                    else{
                        programInfo.getPerfTracker().addCount("rejected_changes",1);
                    }
                    index++;
                }

                ArrayList<Object> requiredForTest = new ArrayList<>();
                requiredForTest.add(bestCuList);
                requiredForTest.add(compPosition);
                requiredForTest.add(copiedUnit);
                if(removedNodes.size()>0&&testChange(bestCuList,compPosition,copiedUnit)){
                    //if changed, remove the nodes we removed from the original ast
                    for(Node x:alterableRemoves){
                        currentNode.remove(x);
                    }

                    minimized=false;
                    copiedList.removeAll(removedNodes);
                    alterableList.removeAll(alterableRemoves);

                    //make another copy and try to run the loop again
                    copiedUnit = bestCuList.get(compPosition).getValue1().clone();
                    copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);

                    copiedList = getCurrentNodeList(copiedNode, alterableList);
                    i=copiedList.size()/2;

                    //TODO:: checkdeterminism fix it please
                    if(checkDeterminism)
                        if(!CheckDeterminism.checkOrCreate(programInfo,currentNode, alterableRemoves,"HDD-"+programInfo.getPerfTracker().getCountForCount("ast_changes"))){
                            //it wasnt true idk, say it was bad or something. bad boy code! work and you will receive cheez its
                            System.out.println("Idk how this happened");
                            System.out.println(currentNode);
                            System.out.println("HDD-"+programInfo.getPerfTracker().getCountForCount("ast_changes"));
                            System.exit(-1);

                        }
                    break;
                } else{
                    copiedUnit = bestCuList.get(compPosition).getValue1().clone();
                    copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
                    copiedList = getCurrentNodeList(copiedNode, alterableList);
                }
            }
        }
        //check changes
        //if they worked REMOVE THE SAME NODES FROM ORIGINAL DONT COPY ANYTHING
    }

    private Node findCurrentNode(Node currentNode, int compPosition, CompilationUnit copiedUnit){

        Node curNode = currentNode;
        List<Node> traverseList = new ArrayList<>();
        traverseList.add(curNode);
        while(!(curNode instanceof CompilationUnit)){
            curNode = curNode.getParentNode().get();
            traverseList.add(0, curNode);
        }

        curNode = copiedUnit;
        traverseList.remove(0);

        while(!traverseList.isEmpty()){
            for(Node x: curNode.getChildNodes()){
                if(x.equals(traverseList.get(0))){
                    if(traverseList.size()==1){
                        return x;
                    }
                    curNode=x;
                    //System.out.println("Found matching: "+ x.getClass().toGenericString()+"      "+traverseList.get(0).getClass().toGenericString());
                    break;
                }
            }
            traverseList.remove(0);
        }

        return null;

    }
    private ArrayList<Node> getCurrentNodeList(Node currentNode, List<Node> list){

        //if(LOG_MESSAGES){
        // System.out.println("Current Node in gCNL: " + currentNode);
        //  }
        List<Node> cloneList = currentNode.getChildNodes();

        ArrayList<Node> childrenWeCareAbout = new ArrayList<>(cloneList);

        childrenWeCareAbout.retainAll(list);
        return childrenWeCareAbout;

    }
}
