package hc.interfaces;

/**
 * Interface for workers that do something to patient (Nurse/Cashier/Doctor)
 */
public interface IServiceWorker {
    boolean isBusy();
    boolean providePatient(IPatient patient);

    void suspend();
    void resume();
}
