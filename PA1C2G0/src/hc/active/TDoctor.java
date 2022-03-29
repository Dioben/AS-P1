package hc.active;

import hc.HCInstance;
import hc.Timer;

public class TDoctor extends TServiceWorker{

    TDoctor(Timer timer, HCInstance instance) {
        super(timer, instance);
    }

    @Override
    void serveCostumer() {
        try {
            sleep(timer.getAppointmentTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
