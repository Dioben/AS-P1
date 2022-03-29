package hc.interfaces;

public interface IServiceWorker {
    boolean isBusy();
    boolean providePatient(IPatient patient);
}
