package hc;

public class Main {

    public static void main(String[] args) {
        TGUI.setGUILook(new String[] { "GTK+", "Nimbus" });
        TGUI gui = new TGUI();
        gui.start();
    }

}