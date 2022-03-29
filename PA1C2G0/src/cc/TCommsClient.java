package cc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCommsClient extends Thread {

    private Socket comms;
    private PrintWriter out;
    private BufferedReader in;
    private String host;
    private int port;

    TCommsClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            comms = new Socket(host, port);
            out = new PrintWriter(comms.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(comms.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] notification = inputLine.split(" ");
                switch (notification[0]) {
                    //TODO: handle incoming messages
                    case "REQ":
                        //TODO: Push notification to UI
                        int id = Integer.parseInt(notification[1]);
                        String destination = notification[2];
                        break;
                    default:
                        break;
                }
            }

            in.close();
            out.close();
            comms.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSim(int adults, int children, int seats, String evalTime, String medicTime, String payTime, String getUpTime) {
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

    public void resumeSim() {
        out.println("RESUME");
    }

    public void pauseSim() {
        out.println("SUSPEND");
    }

    public void stopSim() {
        out.println("STOP");
    }

    public void endSim() {
        out.println("END");
        System.exit(0);
    }

    public void SwapAuto() {
        out.println("SWAP AUTO");
    }

    public void SwapManual() {
        out.println("SWAP MANUAL");
    }

    public void authorize(int id, String to) {
        out.println("AUTH " + id + " " + to);
    }
}
