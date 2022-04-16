package hc.active;

import hc.enums.Severity;
import hc.utils.Timer;
import hc.interfaces.IPatient;
import hc.interfaces.IWorkerRoom;

public class TNurse extends TServiceWorker{

    /**
     * Instance Nurse Worker, this subclass of ServiceWorker waits <i>evaluation</i> time
     * @param timer the wait interval provider class
     * @param surroundings the room this worker is a part of
     */
    public TNurse(Timer timer, IWorkerRoom surroundings) {
        super(timer, surroundings);
    }

    @Override
    /**
     * Wait for a given <i>evaluation</i> time then assign severity
     * @param patient patient will receive a randomly-determined Severity score
     */
    void serveCustomer(IPatient patient) {
        try {
            sleep(timer.getEvaluationTime());
            patient.setSeverity(getSeverity());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Generates patient severity rating, equal probability between Yellow/Red/Blue
     * @return patient Severity Score
     */
    private Severity getSeverity() {
        int roll = (int) (Math.random() * 3); //0 to 2
        if (roll==0){
            return Severity.YELLOW;
        }else if(roll==1){return Severity.RED;}
        return Severity.BLUE;
    }
}
