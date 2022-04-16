package cc;

public class Main {
    /**
     * Launches a client pointed towards localhost 8000 and a UI process
     * @param args Command line arguments, unused
     */
    public static void main(String[] args) throws InterruptedException {
        hc.GUI.setGUILook(new String[] {"GTK+", "Nimbus"});
        new GUI();
    }

}
