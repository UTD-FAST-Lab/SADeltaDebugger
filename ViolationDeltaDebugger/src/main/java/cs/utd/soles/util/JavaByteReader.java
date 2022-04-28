package cs.utd.soles.util;

public class JavaByteReader {
    public static String getMethodSigFromString(String things) {
        String returnString ="";

        String[] parts = things.trim().split(" ");

        int paramIndex=2;
        returnString=parts[1];
        for(int i=0;i<parts.length;i++){
            if(parts[i].contains("(")){
                paramIndex=i;
            }
        }
        parts[paramIndex]= parts[paramIndex].substring(parts[paramIndex].indexOf("(")+1,parts[paramIndex].lastIndexOf(")"));
        //System.out.println(parts[paramIndex]);

        int index= 0;
        while(index<parts[paramIndex].length()){
            char c = parts[paramIndex].charAt(index);

            //isprimitive
            String primitiveString= isPrimitive(c);
            if(primitiveString.length()>0){
                returnString+=primitiveString;
                index++;
            }
            else {
                //not so primitive
                switch (c) {

                    case '[':
                        String primitiveString2 = isPrimitive(parts[paramIndex].charAt(index + 1));
                        if (primitiveString2.length() > 0) {
                            returnString += primitiveString + "[]";
                            index+=2;
                        } else {
                            //not a primitive array.
                            String type = parts[paramIndex].substring(index+2,parts[paramIndex].indexOf(";",index+2)).replace("/",".");
                            returnString+=" "+type+"[]";
                            index+=3+type.length();
                        }
                        break;
                    case 'L':
                        String type = parts[paramIndex].substring(index+1,parts[paramIndex].indexOf(";",index+1)).replace("/",".");
                        returnString+=" "+type;
                        index += 2+type.length();
                        break;
                    default:
                        throw new RuntimeException("Ran into a case not handled for method signatures: " + c);
                }
            }
        }

        return returnString;
    }

    public static String getMethodSigFromGetSig(String sig){
        String returnString = sig.toString();
        returnString = returnString.replace("("," ");
        returnString = returnString.replace(")"," ");
        returnString = returnString.replace(",","");
        returnString = returnString.trim();
        return returnString;
    }

    private static String isPrimitive(char c){
        int index=0;
        String returnString="";
        switch(c) {
            case 'Z':
                returnString += " boolean";
                index++;
                break;
            case 'B':
                returnString += " byte";
                index++;
                break;
            case 'C':
                returnString += " char";
                index++;
                break;
            case 'S':
                returnString += " short";
                index++;
                break;
            case 'I':
                returnString += " int";
                index++;
                break;
            case 'J':
                returnString += " long";
                index++;
                break;
            case 'F':
                returnString += " float";
                index++;
                break;
            case 'D':
                returnString += " double";
                index++;
                break;
        }
        return returnString;
    }
}
