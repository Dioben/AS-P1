package hc.active;

import hc.HCInstance;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

public class TDoctor extends TServiceWorker{

    /**
     * Instance Doctor Worker, this subclass of ServiceWorker waits <i>appointment</i> time
     * @param timer The wait interval provider class
     * @param instance the HC instance this worker is a part of
     * @param surroundings the room this worker is a part of
     */
    public TDoctor(Timer timer, HCInstance instance, IContainer surroundings) {
        super(timer, instance, surroundings);
    }

    @Override
    /**
     * Wait for a given <i>appointment</i> time then return
     * @param patient unchanged
     */
    void serveCustomer(IPatient patient) {
        try {
            sleep(timer.getAppointmentTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
