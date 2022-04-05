package hc.active;

import hc.HCInstance;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;
import hc.interfaces.IWorkerRoom;

public class TCashier extends TServiceWorker{

    /**
     * Instance Cashier Worker, this subclass of ServiceWorker waits <i>payment</i> time
     * @param timer The wait interval provider class
     * @param surroundings the room this worker is a part of
     */
    public TCashier(Timer timer, IWorkerRoom surroundings) {
        super(timer, surroundings);
    }

    /**
     * Wait for a given <i>payment</i> time then return
     * @param patient unchanged
     */
    @Override
    void serveCustomer(IPatient patient) {
        try {
            sleep(timer.getPaymentTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
