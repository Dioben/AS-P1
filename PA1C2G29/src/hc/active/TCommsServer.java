package hc.active;

import hc.TGUI;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Main server class for this project
 * Endlessly accepts connection requests and deploys handler threads
 */
public class TCommsServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private TGUI gui;

    public TCommsServer(int port, TGUI gui) {
        this.port = port;
        this.gui = gui;
    }

    public void run() { // run socket thread creation indefinitely
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                new TCommsHandler(serverSocket.accept(), gui).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
