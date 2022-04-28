package cs.utd.soles.classgraph;

import cs.utd.soles.callgraph.methodgraph.MethodNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DependencyGraph {

    LinkedList<ClassNode> graph = new LinkedList<ClassNode>();

    LinkedList<MethodNode> methodGraph = new LinkedList<>();


    public DependencyGraph(){
        graph = new LinkedList<ClassNode>();
        methodGraph = new LinkedList<MethodNode>();
    }

    //construct this dependency graph from the dot file
    public void parseGraphFromDot(File f, HashMap<String,String> classNamesToPaths) throws FileNotFoundException {
        Scanner sc = new Scanner(f);
        String text = "";
        boolean start=true;
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.contains("(classes)")) {
                if (start) {
                    text += line.replace("(classes)\";", "");
                    start = false;
                } else
                    text += " -> " + line.replace("(classes)\";", "");
            }
        }
        sc.close();
        if(text.length()==0)
            return;
        String[] cut  = text.split("\\s+->\\s+");
        //System.out.println(Arrays.toString(cut));
        for(int i=0;i<cut.length;i++){
            cut[i]=cut[i].replace("\"","").trim();
        }
        //System.out.println(Arrays.toString(cut));
        for(int i=0;i<cut.length;i+=2){

            ClassNode check = new ClassNode(cut[i], classNamesToPaths.get(cut[i]));
            ClassNode node = graph.contains(check)? graph.get(graph.indexOf(check)):check;

            ClassNode dCheck = new ClassNode(cut[i+1],classNamesToPaths.get(cut[i+1]));
            ClassNode dNode = graph.contains(dCheck)? graph.get(graph.indexOf(dCheck)):dCheck;
            //add dependency to node
            node.addDependency(dNode);

            if(check==node) {
                graph.add(node);
            }
            if(dCheck==dNode){
                graph.add(dNode);
            }

            // System.out.println("print node: "+node);
        }

    }

    private HashMap<ClassNode, HashSet<ClassNode>> visited;

    public ArrayList<HashSet<ClassNode>> getTransitiveClosuresDifferent(){
        long start= System.nanoTime();
        ArrayList<HashSet<ClassNode>> returnList = new ArrayList<>();
        HashSet<HashSet<ClassNode>> returnSet = new HashSet<>();
        visited= new HashMap<>();

        for(ClassNode x: graph){
            HashSet<ClassNode> thing = new HashSet<>();
            findClosureForThis(x,thing);
            returnSet.add(thing);
            visited.put(x,thing);
            x.setClosureSize(thing.size());
        }



        //returnSet.addAll(returnList);
        returnList = new ArrayList<HashSet<ClassNode>>(returnSet);
        long end = System.nanoTime()-start;
        System.out.println(end);
        return returnList;
    }

    private void findClosureForThis(ClassNode g, HashSet<ClassNode> closure) {
        //we have completely computed this node before
        if(visited.containsKey(g)){
            closure.addAll(visited.get(g));
            return;
        }else{
            //we ain't got it so we got to compute it,
            closure.add(g);
        }

        for(ClassNode x: g.getDependencies()){

            //recur on unseen dependencies
            if(!closure.contains(x))
                findClosureForThis(x,closure);
        }

    }

    public ClassNode getClassNodeForFilePath(String path){
        if(path==null)
            return null;
        for(ClassNode x: graph){
            if(x.getFilePath()!=null)
            if(x.getFilePath().equals(path))
                return x;

        }
        return null;
    }


    public void makeCallgraphEdge(MethodNode node, MethodNode dNode) {

        MethodNode found = methodGraph.contains(node)? methodGraph.get(methodGraph.indexOf(node)):node;

        MethodNode dFound = methodGraph.contains(dNode)? methodGraph.get(methodGraph.indexOf(dNode)):dNode;

        found.addDependency(dFound);

        if(node==found){
            methodGraph.add(found);
        }
        if(dNode==dFound){
            methodGraph.add(dFound);
        }


    }
}
