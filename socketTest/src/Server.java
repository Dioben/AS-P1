import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket serverSocket;
    public static int count = 0;
    public void start(int port) throws IOException { //run socket thread creation indefinitely
        serverSocket = new ServerSocket(port);
        while (true){
            new EchoClientHandler(serverSocket.accept()).start();
            System.out.println("dispatched a thread");}
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            boolean cleanBreak = false;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (".".equals(inputLine)) {
                    out.println("bye");
                    cleanBreak = true;
                    break;
                }
                Server.count++;
                out.println(Server.count);
            }

            in.close();
            out.close();
            clientSocket.close();
            System.out.println("Client is gone in a "+(cleanBreak?"clean":"dirty"+ " way"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(8000);
    }
}