package hc.active;

import hc.HCInstance;
import hc.HCPLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * Client handler branched off of main simulation
 * Contains a Health Center instance and handles message flow with client
 */
public class TCommsHandler extends Thread {

    //TODO: check whether this logger should even be here
    private HCPLogger logger;
    private Socket comms;
    private String instanceName;
    private PrintWriter out;
    private BufferedReader in;
    private String mode = "AUTO";

    //TODO: this object class is nowhere near done
    private HCInstance instance;

    public TCommsHandler(Socket accept) {
        comms = accept;
        instanceName = UUID.randomUUID().toString();

    }

    /**
     * repeatedly gets next socket instruction and parses it
     * valid commands:
     * START <parameters>
     * RESUME
     * SUSPEND
     * STOP
     * END
     * SWAP <mode>
     * AUTH <patientID>
     */
    public void run() {
        try {
            out = new PrintWriter(comms.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(comms.getInputStream()));
            logger = new HCPLogger(instanceName);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                String[] command = inputLine.split(" ");
                switch (command[0]) {
                    case "START":
                        startInstance(command);
                        break;
                    case "RESUME":
                        if (instance != null)
                            instance.progress();
                        break;
                    case "SUSPEND":
                        if (instance != null)
                            instance.pause();
                        break;
                    case "STOP":
                        if (instance != null)
                            instance.cleanUp();
                        instance = null;
                        break;
                    case "END":
                        if (instance != null)
                            instance.cleanUp(); //probably not strictly necessary
                        System.exit(0);
                    case "SWAP":
                        mode = command[1];
                        if (instance != null)
                            instance.setControls(mode);
                        break;
                    case "AUTH":
                        String patientID = command[1];
                        instance.permitMovement(patientID);
                        break;
                    default:
                        break;
                }
            }

            in.close();
            out.close();
            comms.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * starts an HC instance based on command
     * @param command full command string passed to socket, parsing is done internally
     */
    private void startInstance(String[] command) {
        int adults = Integer.parseInt(command[1]);
        int children = Integer.parseInt(command[2]);
        int seats = Integer.parseInt(command[3]);
        int evalTime = Integer.parseInt(command[4]);
        int medicTime = Integer.parseInt(command[5]);
        int payTime = Integer.parseInt(command[6]);
        int getUpTime = Integer.parseInt(command[7]);
        instance = new HCInstance(adults, children, seats, evalTime, medicTime, payTime, getUpTime,this);
    }

    /**
     * Request permission for a pacient to move in manual mode
     * @param patientID Patient's display info
     * @param to Name of the target room
     */
    public void requestPermission(String patientID, String to) {
        out.println("REQ " + patientID + " " + to);
    }

    /**
     * Tell client that simulation has finished running naturally
     */
    public void notifyDone(){
        out.println("DONE");
    }
}
