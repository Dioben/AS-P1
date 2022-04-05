package cc;

public class Main {
    /**
     * Launches a client pointed towards localhost 8000 and a UI process
     * @param args Command line arguments, unused
     */
    public static void main(String[] args) {
        TCommsClient commsClient = new TCommsClient("localhost", 8000);
        commsClient.start();
        GUI.setGUILook("GTK+");
        GUI gui = new GUI(commsClient);
        gui.start();
    }

}
