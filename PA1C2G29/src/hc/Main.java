package hc;

public class Main {

    /**
     * Launches a HCP server instance with a GUI
     * Port to broadcast to must be established during runtime
     *
     * @param args command line arguments, unused
     */
    public static void main(String[] args) {
        TGUI.setGUILook(new String[] { "GTK+", "Nimbus" });
        TGUI gui = new TGUI();
        gui.start();
    }

}