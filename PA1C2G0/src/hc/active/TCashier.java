package hc.active;

import hc.Timer;
import hc.interfaces.IPatient;
import hc.interfaces.IWorkerRoom;

public class TCashier extends TServiceWorker{

    /**
     * Instance Cashier Worker,<p> this subclass of ServiceWorker waits <i>payment</i> time
     * @param timer The wait interval provider class
     * @param surroundings The room this worker is a part of
     */
    public TCashier(Timer timer, IWorkerRoom surroundings) {
        super(timer, surroundings);
    }

    /**
     * Wait for a given <i>payment</i> time then return
     * @param patient Unchanged
     */
    @Override
    void serveCustomer(IPatient patient) {
        try {
            sleep(timer.getPaymentTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
