package hc.active;

import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;
import hc.interfaces.IWorkerRoom;

/**
 * Generic class for service workers (Nurse/Cashier/Doctor)
 * Implements every method except ServeCustomer()
 */
public abstract class TServiceWorker extends Thread implements hc.interfaces.IServiceWorker {
    protected Timer timer;
    private IWorkerRoom surroundings;
    private IPatient costumer;

    /**
     * Waits for a customer to be assigned to itself
     * serves customer
     * removes customer from self
     * warns container that they're done
     */
    private void handleNextCostumer() {

        while (costumer == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serveCustomer(costumer);
        costumer = null;
        surroundings.notifyDone();//allow patient to getNextRoom
    }

    public TServiceWorker(Timer timer,IWorkerRoom surroundings){
        this.timer = timer;
        this.surroundings = surroundings;
    }

    /**
     * infinitely waits for next patient to show up
     */
    public void run(){
        while(true){
            handleNextCostumer();
        }
    }

    /**
     * Attempts to provide a patient to this worker
     * @param patient the provided patient
     * @return whether the patient was accepted, not expected to fail
     */
    public boolean providePatient(IPatient patient){
        if (!isBusy()){
            this.costumer = patient;
            this.notify();
            return true;
        }
        return false;
    }


    public boolean isBusy(){
        return costumer==null;
    }
    abstract void serveCustomer(IPatient patient);
}
