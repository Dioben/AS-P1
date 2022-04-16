package hc;

public class Main {

    public static void main(String[] args) {
        GUI.setGUILook(new String[] { "GTK+", "Nimbus" });
        GUI gui = new GUI();
        gui.start();
    }

}