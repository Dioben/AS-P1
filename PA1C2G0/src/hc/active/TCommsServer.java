package hc.active;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Endlessly accepts connection requests and deploys handler threads
 */
public class TCommsServer extends Thread {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException { //run socket thread creation indefinitely
        serverSocket = new ServerSocket(port);
        while (true) {
            new TCommsHandler(serverSocket.accept()).start();
        }
    }
}
