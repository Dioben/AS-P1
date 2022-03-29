package hc.active;

import hc.HCInstance;
import hc.Timer;

public class TCashier extends TServiceWorker{

    TCashier(Timer timer, HCInstance instance) {
        super(timer, instance);
    }

    @Override
    void serveCostumer() {
        try {
            sleep(timer.getPaymentTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
