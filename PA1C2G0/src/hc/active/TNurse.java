package hc.active;

import hc.HCInstance;
import hc.enums.Severity;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

public class TNurse extends TServiceWorker{


    public TNurse(Timer timer, HCInstance instance, IContainer surroundings) {
        super(timer, instance, surroundings);
    }

    @Override
    void serveCostumer(IPatient patient) {
        try {
            sleep(timer.getEvaluationTime());
            patient.setSeverity(getSeverity());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Severity getSeverity() {
        int roll = (int) (Math.random() * 3); //0 to 2
        if (roll==0){
            return Severity.YELLOW;
        }else if(roll==1){return Severity.RED;}
        return Severity.BLUE;
    }
}
