package hc.active;

import hc.HCInstance;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

public class TCashier extends TServiceWorker{


    public TCashier(Timer timer, HCInstance instance, IContainer surroundings) {
        super(timer, instance, surroundings);
    }

    @Override
    void serveCostumer(IPatient patient) {
        try {
            sleep(timer.getPaymentTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
