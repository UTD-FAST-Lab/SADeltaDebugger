package cs.utd.soles.classgraph;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import cs.utd.soles.callgraph.methodgraph.MethodNode;
import cs.utd.soles.classgraph.ClassNode;
import org.javatuples.Pair;

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
    public DependencyGraph(File f, ArrayList<Pair<File, CompilationUnit>> originalCUs) throws FileNotFoundException {
        graph = new LinkedList<ClassNode>();
        methodGraph = new LinkedList<MethodNode>();
        fillNamesToPaths(originalCUs);

        Scanner sc = new Scanner(f);
        String text = "";
        boolean start=true;
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(!line.contains("(not found)") && line.contains("->")) {
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


            ClassNode node =null;
            ClassNode dNode =null;
            ClassNode check=null;
            ClassNode dCheck=null;
            //map an internal dependency
            if(classNamesToPaths.get(cut[i])!=null) {
                check = new ClassNode(cut[i], classNamesToPaths.get(cut[i]));
                node = graph.contains(check) ? graph.get(graph.indexOf(check)) : check;


            }
            if(classNamesToPaths.get(cut[i+1])!=null) {
                dCheck = new ClassNode(cut[i + 1], classNamesToPaths.get(cut[i + 1]));
                dNode = graph.contains(dCheck) ? graph.get(graph.indexOf(dCheck)) : dCheck;
            }

            if(node!=null&&dNode!=null) {
                //add dependency to node
                node.addDependency(dNode);
            }
            if(check!=null)
                if(check==node) {
                    graph.add(node);
                }
            if(dCheck!=null)
                if(dCheck==dNode){
                    graph.add(dNode);
                }

            // System.out.println("print node: "+node);
        }

    }



    private HashMap<String, String> classNamesToPaths;



    public void findClasses(Node cur, String fileName){

        //this node is a class

        Optional<PackageDeclaration> fullName = ((CompilationUnit) cur).getPackageDeclaration();
        //either get fullName or just defualt to className
        String name = fullName.isPresent()? fullName.get().getNameAsString(): "";
        if(!name.isEmpty())
            name=name+"."+fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf(".java"));
        else{
            name=fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf(".java"));
        }
        classNamesToPaths.put(name, fileName);

    }
    private void fillNamesToPaths(ArrayList<Pair<File,CompilationUnit>> originalCUnits){
        classNamesToPaths = new HashMap<>();

        for(Pair x: originalCUnits){
            findClasses((Node)x.getValue1(), ((File)x.getValue0()).getAbsolutePath());
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
