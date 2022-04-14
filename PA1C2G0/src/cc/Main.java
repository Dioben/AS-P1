package cc;

public class Main {
    /**
     * Launches a client pointed towards localhost 8000 and a UI process
     * @param args Command line arguments, unused
     */
    public static void main(String[] args) throws InterruptedException {
        GUI.setGUILook("GTK+");
        GUI gui = new GUI();
        gui.start();
    }

}
