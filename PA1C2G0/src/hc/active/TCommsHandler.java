package hc.active;

import hc.GUI;
import hc.HCInstance;
import hc.utils.MHCPLogger;
import hc.interfaces.ILogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Client handler branched off of main simulation
 * <p>
 * Contains a Health Center instance and handles message flow with client
 */
public class TCommsHandler extends Thread {

    private final Socket comms;
    private PrintWriter out;
    private String mode = "AUTO";
    private final ReentrantLock writeLock; // socket writers are supposed to be thread safe but let's make sure
    private HCInstance instance;
    private final GUI gui;
    private final ILogger logger;

    public TCommsHandler(Socket accept, GUI gui) {
        comms = accept;
        writeLock = new ReentrantLock();
        this.gui = gui;
        logger = new MHCPLogger();
    }

    /**
     * Repeatedly gets next socket instruction and parses it
     * <p>
     * Valid commands:
     * <p>
     * START {@literal <Parameters>}
     * <p>
     * RESUME
     * <p>
     * SUSPEND
     * <p>
     * STOP
     * <p>
     * END
     * <p>
     * SWAP {@literal <Mode>}
     * <p>
     * AUTH {@literal <Room ID>}
     */
    public void run() {
        try {
            out = new PrintWriter(comms.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(comms.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] command = inputLine.split(" ");
                switch (command[0]) {
                    case "START":
                        startInstance(command);
                        break;
                    case "RESUME":
                        if (instance != null) {
                            gui.setStateLabel("Running");
                            logger.printState("RUNNING");
                            instance.progress();
                        }
                        break;
                    case "SUSPEND":
                        if (instance != null) {
                            instance.pause();
                            gui.setStateLabel("Suspended");
                            logger.printState(command[0]);
                        }
                        break;
                    case "STOP":
                        if (instance != null) {
                            instance.cleanUp();
                            gui.setStateLabel("Stopped");
                            logger.printState(command[0]);
                        }
                        instance = null;
                        break;
                    case "END":
                        if (instance != null)
                            instance.cleanUp(); // probably not strictly necessary
                        logger.printState(command[0]);
                        System.exit(0);
                    case "SWAP":
                        mode = command[1];
                        logger.printState(mode);
                        if (instance != null)
                            instance.setControls(mode);
                        break;
                    case "AUTH":
                        String roomID = command[1];
                        instance.permitNotification(roomID);
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
     * Creates and starts an HC instance based on command
     * 
     * @param command full command string passed to socket, parsing is done
     *                internally
     */
    private void startInstance(String[] command) {
        if (instance != null)
            instance.cleanUp();
        int adults = Integer.parseInt(command[1]);
        int children = Integer.parseInt(command[2]);
        int seats = Integer.parseInt(command[3]);
        int evalTime = Integer.parseInt(command[4]);
        int medicTime = Integer.parseInt(command[5]);
        int payTime = Integer.parseInt(command[6]);
        int getUpTime = Integer.parseInt(command[7]);
        gui.setSeatCount(seats);
        instance = new HCInstance(adults, children, seats, evalTime, medicTime, payTime, getUpTime, this,
                mode.equals("MANUAL"), gui, logger);
        gui.setStateLabel("Running");
        logger.printState("RUNNING");
        instance.start();
    }

    /**
     * Request permission for a patient to move in manual mode
     * 
     * @param roomName name of room requesting movement
     */
    public void requestPermission(String roomName) {
        writeLock.lock();
        out.println("REQ " + roomName);
        writeLock.unlock();
    }

    /**
     * Notify client that simulation has finished running naturally
     */
    public void notifyDone() {
        writeLock.lock();
        out.println("DONE");
        writeLock.unlock();
        instance.cleanUp();
        logger.printState("STOP");
        gui.setStateLabel("Finished");
    }
}
