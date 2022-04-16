package hc.interfaces;

/**
 * Interface for rooms containing some form of worker (Nurse/Cashier/Doctor)
 */
public interface IWorkerRoom extends IRoom {
    /**
     * Tell this container that task within has been conducted
     */
    void notifyDone();

    boolean canEnter();

}
