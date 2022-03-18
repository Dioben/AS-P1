import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost",8000);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        for(int i=0;i<15;i++){
         out.println("hello bastard");
         sleep(1000);
            System.out.println("Got " + in.readLine()+ " from server");
        }
        out.println(".");
        sleep(1000);
        System.out.println("Got " + in.readLine()+ " from server");

    }
}