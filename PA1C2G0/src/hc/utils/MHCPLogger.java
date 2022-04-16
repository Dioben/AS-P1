package hc.utils;

import hc.interfaces.ILogger;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class MHCPLogger implements ILogger {
    private PrintWriter fileWriter;
    private final ReentrantLock loggerAccess;
    public MHCPLogger(){
        String fileName = "logs/log.txt";
        (new File(fileName)).getParentFile().mkdirs(); //write folders up to this point
        try {
            fileWriter = new PrintWriter(new FileWriter(fileName), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loggerAccess = new ReentrantLock();

    };

    /**
     * Prints the initial log header
     * @param adults number of adults (NoA)
     * @param children number of children (NoC)
     * @param seats number of seats (NoS)
     */
    public void  printHeader(int adults, int children, int seats){

        String output = "NoA:"+adults+", NoC:"+children+", NoS: "+seats+"\n"+
                "STT | ETH ET1 ET2 | EVR1 EVR2 EVR3 EVR4 | WTH WTR1 WTR2 | MDH MDR1 MDR2 MDR3 MDR4 | PYH | OUT";
        loggerAccess.lock();
        fileWriter.println(output);
        System.out.println(output);
        loggerAccess.unlock();

    }

    /**
     * Prints a position update for a given patient string in a given header column
     * @param place header column
     * @param patient patient string
     */
    public void printPosition(String place, String patient){
        StringBuilder outputString = new StringBuilder();
        outputString.append("    | "); //STT is always empty in these
        for (String header: new String[]{"ETH","ET1","ET2"}){
            if (place.equals(header))
                outputString.append(formatPosition(place,patient));
            else
                outputString.append(formatPosition(header,null));
        }
        outputString.append("| "); //Transition to EVR
        for (String header: new String[]{"EVR1","EVR2","EVR3","EVR4"}){
            if (place.equals(header))
                outputString.append(formatPosition(place,patient));
            else
                outputString.append(formatPosition(header,null));
        }
        outputString.append("| "); //Transition to WTH
        for (String header: new String[]{"WTH","WTR1","WTR2"}){
            if (place.equals(header))
                outputString.append(formatPosition(place,patient));
            else
                outputString.append(formatPosition(header,null));
        }
        outputString.append("| "); //Transition to MDH
        for (String header: new String[]{"MDH","MDR1","MDR2","MDR3","MDR4"}){
            if (place.equals(header))
                outputString.append(formatPosition(place,patient));
            else
                outputString.append(formatPosition(header,null));
        }
        outputString.append("| "); //Transition to PYN
        if (place.equals("PYH"))
            outputString.append(formatPosition(place,patient));
        else
            outputString.append(formatPosition("PYH",null));
        outputString.append("| "); //Transition to OUT
        if (place.equals("OUT"))
            outputString.append(formatPosition(place,patient));
        else
            outputString.append(formatPosition("OUT",null));
        String content = outputString.toString();
        loggerAccess.lock();
        fileWriter.println(content);
        System.out.println(content);
        loggerAccess.unlock();
    }

    private String formatPosition(String header, String patient) {
        patient = (patient==null)?"    ":patient.toUpperCase(); //if null auto pads to 4, smallest header is 3 + 1 trailing space
        int len = patient.length();
        for (int i = header.length()+1-len;i>0;i--){
            patient+= " ";
        }
        return patient;
    }

    /**
     * Prints a given state change notification
     * @param state state name
     */
    public void printState(String state){
        String output = state.toUpperCase().substring(0,3)+" |             |                     |               |                         |     |    ";
        loggerAccess.lock();
        fileWriter.println(output);
        System.out.println(output);
        loggerAccess.unlock();
    }

}
