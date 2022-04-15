package hc.active;

import hc.HCInstance;
import hc.Timer;
import hc.enums.Severity;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;
import hc.interfaces.IWorkerRoom;

public class TDoctor extends TServiceWorker{

    /**
     * Instance Doctor Worker, this subclass of ServiceWorker waits <i>appointment</i> time
     * @param timer The wait interval provider class
     * @param surroundings the room this worker is a part of
     */
    public TDoctor(Timer timer, IWorkerRoom surroundings) {
        super(timer, surroundings);
    }

    @Override
    /**
     * Wait for a given <i>appointment</i> time then return
     * @param patient severity is set to UNASSIGNED
     */
    void serveCustomer(IPatient patient) {
        try {
            sleep(timer.getAppointmentTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        patient.setSeverity(Severity.UNASSIGNED);
    }
}
