package HC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;


public class TCommsHandler extends Thread {

    private final Socket comms;
    private final String instanceName;
    private PrintWriter out;
    private BufferedReader in;
    private String mode = "AUTO";

    //TODO: this object class is functionally empty
    private HCInstance instance;

    public TCommsHandler(Socket accept) {
        comms = accept;
        instanceName = UUID.randomUUID().toString();

    }

    public void run() {
        try {
            out = new PrintWriter(comms.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(comms.getInputStream()));

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
                        int patientID = Integer.parseInt(command[1]);
                        String destination = command[2];
                        instance.permitMovement(patientID, destination);
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

    private void startInstance(String[] command) {
        int adults = Integer.parseInt(command[1]);
        int children = Integer.parseInt(command[2]);
        int seats = Integer.parseInt(command[3]);
        String evalTime = command[4]; //parse these in the randomGenerator class
        String medicTime = command[5];
        String payTime = command[6];
        String getUpTime = command[7];
        //consider using a Builder here?
        instance = new HCInstance(adults, children, seats, evalTime, medicTime, payTime, getUpTime);
    }

    public void requestPermission(int patientID, String from, String to) {
        //TODO: MAYBE ADD MORE PATIENT INFO, NAMELY ARMBAND COLOR? -> should probably be in notifyMovement?
        out.println("REQ " + patientID + " " + from + " " + to);
    }

    public void notifyMovement(int patientID, String from, String to) {
        //TODO: DETERMINE WHETHER THIS WILL BE NECESSARY
    }
}
