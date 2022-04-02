package hc.places;

import hc.active.TCommsHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCenter extends Thread{

    private final TCommsHandler comms;
    private final ReentrantLock rl;
    private final Condition evrAvailable;
    private final Condition wtrAvailable;
    private final Condition mdwAvailable;
    private final Condition mdrAvailable;
    private boolean manual;

    public MCallCenter(boolean manual, TCommsHandler tCommsHandler){
        rl = new ReentrantLock();
        evrAvailable = rl.newCondition();
        wtrAvailable = rl.newCondition();
        mdwAvailable = rl.newCondition();
        mdrAvailable = rl.newCondition();
        this.manual = manual;
        comms = tCommsHandler;

    }


    public void setManual(boolean b) {
    manual = b;
    }

    public void registerRequest(){//TODO: PARAMS FOR THIS
        if (! manual){
            //TODO: ADD TO WORK QUEUE
          return;
        }
        comms.requestPermission(1,"","");
    }

    public void run(){
        while (true){
            //TODO: PROCESS NEXT ITEM IN WORK QUEUE
        }
    }
}
