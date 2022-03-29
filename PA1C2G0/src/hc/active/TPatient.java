package hc.active;

import hc.Severity;
import hc.Timer;
import hc.interfaces.IContainer;

public class TPatient extends Thread{
    private IContainer surroundings;
    private Severity severity;
    private Timer timer;
    private final boolean child;
    private int displayNumber;
    private int entranceNumber;
    private int paymentNumber;
    private int waitingNumber;
    private String displayValue;
    TPatient(boolean isChild,Timer timer){//add start room to this constructor
        this.severity = Severity.UNASSIGNED;
        this.child = isChild;
        this.timer = timer;
        displayValue = "";
    }
    public void run(){
        while(surroundings != null){
            tryMove();
        }
    }

    private void tryMove(){
        //TODO: WAIT UNTIL CONTAINER ALLOWS YOU TO GET THE FOLLOWING ROOM
        IContainer next = surroundings.getFollowingContainer();// this blocks until the current container is done with you
        try {
            sleep(timer.getMovementTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        next.tryEnter(this);
        this.surroundings.leave();
        this.surroundings = next;

    }

    public void setEntranceNumber(int entranceNumber) {
        this.entranceNumber = entranceNumber;
        this.displayValue = String.valueOf(entranceNumber);
    }
    public void setWaitingNumber(int waitingNumber){
        this.waitingNumber = waitingNumber;
        this.displayValue = String.valueOf(waitingNumber);
    }
    public void setPaymentNumber(int paymentNumber){
        this.paymentNumber = paymentNumber;
        this.displayValue = String.valueOf(paymentNumber);
    }
    public void setSeverity(Severity severity){
        this.severity=severity;
    }

}
