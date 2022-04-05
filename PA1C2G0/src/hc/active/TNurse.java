package hc.active;

import hc.HCInstance;
import hc.enums.Severity;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

public class TNurse extends TServiceWorker{

    /**
     * Instance Nurse Worker, this subclass of ServiceWorker waits <i>evaluation</i> time
     * @param timer The wait interval provider class
     * @param instance the HC instance this worker is a part of
     * @param surroundings the room this worker is a part of
     */
    public TNurse(Timer timer, HCInstance instance, IContainer surroundings) {
        super(timer, instance, surroundings);
    }

    @Override
    /**
     * Wait for a given <i>evalution</i> time then assign severity
     * @param patient unchanged
     */
    void serveCustomer(IPatient patient) {
        try {
            sleep(timer.getEvaluationTime());
            patient.setSeverity(getSeverity());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates patient severity rating, equal probability between Yellow/Red/Blue
     * @return patient severity
     */
    private Severity getSeverity() {
        int roll = (int) (Math.random() * 3); //0 to 2
        if (roll==0){
            return Severity.YELLOW;
        }else if(roll==1){return Severity.RED;}
        return Severity.BLUE;
    }
}
