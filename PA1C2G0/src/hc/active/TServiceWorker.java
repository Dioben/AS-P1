package hc.active;

import hc.HCInstance;
import hc.Timer;

public abstract class TServiceWorker extends Thread{
    protected Timer timer; //TODO: ASK WHETHER THIS IS OK
    private HCInstance hc; //gotta be able to check if we're paused
    //private Container surroundings;
    private TPatient costumer;
    TServiceWorker(Timer timer, HCInstance instance){
        this.timer = timer;
        this.hc = instance;
    }
    public void run(){
        while(true){
            handleNextCostumer();
        }
    }

    private void handleNextCostumer() {

        while(costumer == null || hc.isPaused()){//TODO: PAUSING MAY BE MORE COMPLEX THAN THIS
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serveCostumer();
        //surroundings.notifyServed();//allow patient to getNextRoom
    }

    abstract void serveCostumer();
}
