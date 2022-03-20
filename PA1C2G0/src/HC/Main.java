package HC;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new TCommsServer().start(8000);
        GUI.setGUILook("Nimbus");
        GUI gui = new GUI();
        gui.start();
    }

}