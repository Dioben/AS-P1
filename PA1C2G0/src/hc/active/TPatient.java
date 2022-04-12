package hc.active;

import hc.enums.Severity;
import hc.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

/**
 * Patient thread that moves through rooms
 * Rooms store information about what is done in themselves and what the next step is
 * When next room does not exist Patient thread will exit
 */
public class TPatient extends Thread implements IPatient {
    /**
     * Room Patient is currently in
     */
    private IContainer surroundings;
    private Severity severity=Severity.UNASSIGNED;
    private Timer timer;
    private final boolean child;
    private int entranceNumber;
    private int paymentNumber;
    private int waitingNumber;
    private int roomNumber;
    private String displayValue="";
    public TPatient(boolean isChild, Timer timer, IContainer surroundings){
        this.child = isChild;
        this.timer = timer;
        this.surroundings = surroundings;
    }

    /**
     * Try to advance as long as the current room is not null
     */
    public void run(){
        while(surroundings != null){
            tryMove();
        }
    }

    /**
     * Gets next room from their current environment
     * This is blocking until the room thinks Patient should leave
     * Then moves between rooms
     * And warns previous room that they have left
     */
    private void tryMove(){
        IContainer next = surroundings.getFollowingContainer(this);// this blocks until the current container is done with you
        try {
            sleep(timer.getMovementTime());
            next.enter(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.surroundings.leave(this);
        this.surroundings = next;

    }

    /**
     * Used at entrance hall to set number
     * Automatically updates UI/logs display value
     * @param entranceNumber the assigned number
     */
    public void setEntranceNumber(int entranceNumber) {
        this.entranceNumber = entranceNumber;
        this.displayValue = String.valueOf(entranceNumber);
    }

    /**
     * Used at waiting hall to set number
     * Automatically updates UI/logs display value
     * @param waitingNumber the assigned number
     */
    public void setWaitingNumber(int waitingNumber){
        this.waitingNumber = waitingNumber;
        this.displayValue = String.valueOf(waitingNumber);
    }

    /**
     * Used at payment hall to set number
     * Automatically updates UI/logs display value
     * @param paymentNumber the assigned number
     */
    public void setPaymentNumber(int paymentNumber){
        this.paymentNumber = paymentNumber;
        this.displayValue = String.valueOf(paymentNumber);
    }

    /**
     * Used by Nurse entity after evaluation to set user status
     * @param severity User condition severity
     */
    public void setSeverity(Severity severity){
        this.severity=severity;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    public boolean isChild() {
        return child;
    }

    public String getDisplayValue() {
        String prefix = this.child?"C":"A";
        String suffix = "";
        if (severity == Severity.BLUE)
            suffix = "B";
        if (severity == Severity.YELLOW)
            suffix = "y";
        if (severity == Severity.RED)
            suffix = "R";
        return prefix+displayValue+suffix;
    }

    @Override
    public int getRoomNumber() {
        return roomNumber;
    }

    @Override
    public void setRoomNumber(int entered) {
        roomNumber = entered;

    }

}
