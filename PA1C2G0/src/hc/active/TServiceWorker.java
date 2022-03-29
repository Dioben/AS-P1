package hc.active;

import hc.HCInstance;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

public abstract class TServiceWorker extends Thread implements hc.interfaces.IServiceWorker {
    protected Timer timer; //TODO: ASK WHETHER THIS IS OK
    private HCInstance hc; //gotta be able to check if we're paused
    private IContainer surroundings;
    private IPatient costumer;

    private void handleNextCostumer() {

        while (costumer == null || hc.isPaused()) {//TODO: PAUSING MAY BE MORE COMPLEX THAN THIS
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serveCostumer(costumer);
        costumer = null;
        surroundings.notifyDone();//allow patient to getNextRoom
    }
    public TServiceWorker(Timer timer, HCInstance instance,IContainer surroundings){
        this.timer = timer;
        this.hc = instance;
        this.surroundings = surroundings;
    }
    public void run(){
        while(true){
            handleNextCostumer();
        }
    }

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

    abstract void serveCostumer(IPatient patient);
}
