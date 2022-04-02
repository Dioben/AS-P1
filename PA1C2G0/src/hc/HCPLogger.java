package hc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

public class HCPLogger {
    private PrintWriter fileWriter;
    public HCPLogger(String simId) throws FileNotFoundException {
        //File file = new File("/logs/log"+simId+".txt"); //suited for multiple client system, doesn't match our specs
        File file = new File("/logs/log.txt");
        file.getParentFile().mkdirs(); //write folders up to this point
            fileWriter = new PrintWriter(file);

    };

    public synchronized void  printHeader(int adults, int children, int seats){
        String output = "NoA:"+adults+", NoC:"+children+", NoS: "+seats+"\n"+
                "STT | ETH ET1 ET2 | EVR1 EVR2 EVR3 EVR4 | WTH WTR1 WTR2 | MDH MDR1 MDR2 MDR3 MDR4 | PYH | OUT";
        fileWriter.println(output);
        System.out.println(output);

    }
    public synchronized void printPositions(Map<String,String> places){
        StringBuilder outputString = new StringBuilder();
        outputString.append("    | "); //STT is always empty in these
        outputString.append(formatPosition(places, "ETH"));
        outputString.append(formatPosition(places, "ET1"));
        outputString.append(formatPosition(places, "ET2"));
        outputString.append("| "); //Transition to EVR
        outputString.append(formatPosition(places, "EVR1"));
        outputString.append(formatPosition(places, "EVR2"));
        outputString.append(formatPosition(places, "EVR3"));
        outputString.append(formatPosition(places, "EVR4"));
        outputString.append("| "); //Transition to WTH
        outputString.append(formatPosition(places, "WTH"));
        outputString.append(formatPosition(places, "WTR1"));
        outputString.append(formatPosition(places, "WTR2"));
        outputString.append("| "); //Transition to MDH
        outputString.append(formatPosition(places, "MDH"));
        outputString.append(formatPosition(places, "MDR1"));
        outputString.append(formatPosition(places, "MDR2"));
        outputString.append(formatPosition(places, "MDR3"));
        outputString.append(formatPosition(places, "MDR4"));
        outputString.append("| "); //Transition to PYN
        outputString.append(formatPosition(places, "PYN"));
        outputString.append("| "); //Transition to OUT
        outputString.append(formatPosition(places, "OUT"));
        String content = outputString.toString();
        fileWriter.println(content);
        System.out.println(content);
    }

    private String formatPosition(Map<String, String> places, String header) {
        String spot = places.get(header);
        spot = (spot==null)?"    ":spot.toUpperCase(); //if null auto pads to 4, smallest header is 3 + 1 trailing space
        for (int i = header.length()+1-spot.length();i>0;i--){
            spot+= " ";
        }
        return spot;
    }

    public synchronized void printState(String state){
        String output = state.toUpperCase()+" |             |                     |               |                         |     |    ";
        fileWriter.println(output);
        System.out.println(output);
    }

}
