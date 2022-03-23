package HC;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        GUI.setGUILook("GTK+");
        GUI gui = new GUI();
        gui.start();
        new TCommsServer().start(8000);
    }

}