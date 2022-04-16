package cc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Class responsible for sending messages to the server and parsing received messages
 */
public class TCommsClient extends Thread {

    private Socket comms;
    private PrintWriter out;
    private BufferedReader in;
    private String host;
    private int port;
    private GUI gui;

    /**Instances a comms client that connects to a remote HCP server
     *
     * @param host host of the server, ex: localhost, google.com
     * @param port host server port number
     */
    TCommsClient(String host, int port, GUI gui) {
        this.host = host;
        this.port = port;
        this.gui = gui;
    }

    /**
     * Establishes connection to server and attempts to parse any data received
     * Receives movement requisitions (REQ  {@literal <ROOM>}; ) or finish notifications (DONE)
     */
    @Override
    public void run() {
        try {
            comms = new Socket(host, port);
            out = new PrintWriter(comms.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(comms.getInputStream()));
            gui.connectionStatus(true);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] notification = inputLine.split(" ");
                switch (notification[0]) {
                    case "REQ":
                        String destination = notification[1];
                        gui.putRequest(destination);
                        break;
                    case "DONE":
                        gui.setStatusLabel("Finished");
                        gui.setStopUIState();
                        break;
                    default:
                        break;
                }
            }

            in.close();
            out.close();
            comms.close();
        } catch (IOException e) {
            gui.connectionStatus(false);
            e.printStackTrace();
        }
    }

    /**
     * Sends a "start simulation" message to server
     * @param adults number of adults in simulation
     * @param children number of children in simulation
     * @param seats number of seats in ETH
     * @param evalTime time required for nurse evaluation
     * @param medicTime time required for doctor appointment
     * @param payTime time required for payment
     * @param getUpTime time required for getting up and moving between rooms
     */
    public void startSim(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime) {
        String msg = "START " +
                adults + " " +
                children + " " +
                seats + " " +
                evalTime + " " +
                medicTime + " " +
                payTime + " " +
                getUpTime;
        out.println(msg);
    }

    /**
     * Unpauses simulation
     */
    public void resumeSim() {
        out.println("RESUME");
    }

    /**
     * Pauses simulation
     */
    public void pauseSim() {
        out.println("SUSPEND");
    }

    /**
     * Stops simulation
     */
    public void stopSim() {
        out.println("STOP");
    }

    /**
     * Stops simulation, kills both processes
     */
    public void endSim() {
        out.println("END");
        System.exit(0);
    }

    /**
     * Sets control mode to automatic
     */
    public void SwapAuto() {
        out.println("SWAP AUTO");
    }

    /**
     * Sets control mode to manual
     */
    public void SwapManual() {
        out.println("SWAP MANUAL");
    }

    /**
     * Authorize a given room to notify their vacancy
     * @param id the name of the authorized room
     */
    public void authorize(String id) {
        out.println("AUTH " + id);
    }
}
