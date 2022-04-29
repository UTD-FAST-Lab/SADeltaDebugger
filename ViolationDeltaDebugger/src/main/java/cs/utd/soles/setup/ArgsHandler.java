package cs.utd.soles.setup;


import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArgsHandler{

    HashMap<String, Object> argValues=new HashMap<>();
    public ArgsHandler(String[] args, SetupClass thing){
        argValues.put("RUN_PREFIX","");
        for(int i=0;i<args.length;i++) {
            if(args[i].equals("-src")){
                i++;
                while(!args[i+1].matches("-.+")){
                    thing.addSrcDir(Paths.get(args[i]).toFile());
                    i++;
                }
                thing.addSrcDir(Paths.get(args[i]).toFile());
            }
            if(args[i].equals("-apk")){
                thing.setApkFile(Paths.get(args[i+1]).toFile());
                i++;
            }

            if(args[i].equals("-vs")){
                thing.setTestScriptFile(Paths.get(args[i+1]).toFile());
                i++;
            }
            if(args[i].equals("-bs")){
                thing.setBuildScriptFile(Paths.get(args[i+1]).toFile());
                i++;
            }
            if (args[i].equals("-l")) {
                argValues.put("LOG",true);
            }
            if(args[i].equals("-c")){
                argValues.put("CLASS_REDUCTION",true);
            }
            if(args[i].equals("-m")){
                argValues.put("METHOD_REDUCTION",true);
            }
            if(args[i].equals("-hdd")){
                argValues.put("REGULAR_REDUCTION",true);
            }
            if(args[i].equals("-no_opt")){
                argValues.put("NO_OPTIMIZATION",true);
            }
            if(args[i].equals("-p")){
                String prefix=args[i+1];
                prefix = prefix.replace("/","-");
                argValues.put("RUN_PREFIX",prefix+"_");
                i++;
            }
            if(args[i].equals("-t")){
                argValues.put("TIMEOUT_TIME_MINUTES",Integer.parseInt(args[i+1]));
                i++;
            }
            if(args[i].equals("-bt")){
                argValues.put("BINARY_TIMEOUT_TIME_MINUTES",Integer.parseInt(args[i+1]));
                i++;
            }
            if(args[i].equals("-check_d")){
                argValues.put("CHECK_DETERMINISM",true);
            }
            //add other args here if we want em
        }
    }
    public Optional<Object> getValueOfArg(String arg){
        return Optional.ofNullable(argValues.get(arg));
    }
    public String printArgValues(){
        String returnString="";
        for(Map.Entry<String, Object> e: argValues.entrySet()){
            returnString+=e.getKey().toString()+": "+e.getValue().toString()+"\n";
        }
        return returnString;
    }
}
