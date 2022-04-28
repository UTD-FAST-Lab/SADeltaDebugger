package cs.utd.soles.classgraph;

import java.util.LinkedList;
import java.util.Objects;

public class ClassNode {

    LinkedList<ClassNode> dependencies;

    public int getClosureSize() {
        return closureSize;
    }

    public void setClosureSize(int closureSize) {
        this.closureSize = closureSize;
    }

    private int closureSize=1;

    private String name = "";

    private String filePath="";



    public ClassNode(String name, String filePath){
        this.name=name;
        this.filePath=filePath;
        dependencies = new LinkedList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public void addDependency(ClassNode n){
        dependencies.addLast(n);
    }

    public LinkedList<ClassNode> getDependencies(){
        return dependencies;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof ClassNode)){
            return false;
        }
        return (((ClassNode)o).name.equals(name));
    }

    public void mergeNodes(ClassNode check) {
        LinkedList<ClassNode> checkList = check.getDependencies();

        checkList.removeAll(dependencies);
        dependencies.addAll(checkList);

    }

    public String getName(){
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString(){
        return name +" "+getFilePath()/*+" dependencies: "+dependencies+"\n"*/;
    }
    public String fullPrint(){
        return name+" dependencies: "+dependencies+"\n";
    }

}
