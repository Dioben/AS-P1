package hc.active;

import hc.HCInstance;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

public class TDoctor extends TServiceWorker{


    public TDoctor(Timer timer, HCInstance instance, IContainer surroundings) {
        super(timer, instance, surroundings);
    }

    @Override
    void serveCostumer(IPatient patient) {
        try {
            sleep(timer.getAppointmentTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
