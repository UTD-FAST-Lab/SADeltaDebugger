/*package cs.utd.soles.aqlrunner;

import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flow;
import cs.utd.soles.Runner;
import cs.utd.soles.schema.SchemaGenerator;
import cs.utd.soles.setup.SetupClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class AQLStringHandler {

    public static boolean handleAQL(SetupClass info, String config1String, String config2String, String changeNum){
        try {
            File config1File = turnAQLStringToFile(info, "1", config1String, changeNum);
            File config2File = turnAQLStringToFile(info, "2", config2String, changeNum);

            if(config1File==null||config2File==null) {
                System.out.println("Aborted cause one of files was null");
                return false;
            }
            ArrayList<Flow> flowList = new ArrayList<>();
            if(info.isViolationOrNot()&&info.isTargetType()){
                flowList.addAll(getFlowStrings(config2File));
                flowList.removeAll(getFlowStrings(config1File));
            }else if(info.isViolationOrNot()&&!info.isTargetType()){
                flowList.addAll(getFlowStrings(config1File));
                flowList.removeAll(getFlowStrings(config2File));
            }else {
                flowList.addAll(getFlowStrings(config2File));
                flowList.addAll(getFlowStrings(config1File));
            }
            boolean returnVal=false;

            ArrayList<Flow> checkList = new ArrayList<>();
            checkList.addAll(info.getThisViolation().getConfig1_FlowList());
            checkList.addAll(info.getThisViolation().getConfig2_FlowList());
            checkList.removeAll(flowList);

            if(checkList.size()==0){
                returnVal=true;
            }
            return returnVal;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static File turnAQLStringToFile(SetupClass info, String identity, String aqlString, String changenum) throws IOException {

        //TODO:: add change # to this file
        String time = Long.toHexString(System.currentTimeMillis());
        String fp = "debugger/tempfiles/aqlfiles/"+info.getThisRunName()+"_"+changenum+"_"+time+"out"+identity+".xml";

        File f = Paths.get(fp).toFile();
        f.mkdirs();
        if(f.exists())
            f.delete();
        f.createNewFile();

        String xmlString ="";
        if(aqlString.contains("<answer/>")){
            xmlString ="<answer>\n</answer>";
        }else if(aqlString.contains("<answer>")){
            xmlString = aqlString.substring(aqlString.indexOf("<answer>"), aqlString.indexOf("</answer>") + 9);
        }else{
            return null;
        }
        String header = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n";
        FileWriter fw = new FileWriter(f);
        fw.write(header);
        fw.write(xmlString);
        fw.flush();
        fw.close();

        return f;
    }

    public static ArrayList<Flow> getFlowStrings(File xmlFile){
        AQLFlowFileReader aff = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);
        Iterator<Flow> flowIt = aff.getFlows(xmlFile);
        ArrayList<Flow> out = new ArrayList<Flow>();

        //maybe deduplicate here to keep consistent with Austin
        System.out.println("FLOWS FOR THIS XML FILE: " + xmlFile.getName());
        while(flowIt.hasNext()){
            Flow x = flowIt.next();
            out.add(x);
            System.out.println(x.getSink().getStatementFull() + " " + x.getSink().getMethod());
            System.out.println(x.getSource().getStatementFull() + " " + x.getSource().getMethod());
        }
        return out;
    }
}*/
