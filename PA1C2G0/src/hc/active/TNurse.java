package hc.active;

import hc.HCInstance;
import hc.Timer;

public class TNurse extends TServiceWorker{

    TNurse(Timer timer, HCInstance instance) {
        super(timer, instance);
    }

    @Override
    void serveCostumer() {
        try {
            sleep(timer.getEvaluationTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
