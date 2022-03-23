package CC;

public class Main {

    public static void main(String[] args) {
        TCommsClient commsClient = new TCommsClient("localhost", 8000);
        commsClient.start();
        GUI.setGUILook("GTK+");
        GUI gui = new GUI(commsClient);
        gui.start();
    }

}
