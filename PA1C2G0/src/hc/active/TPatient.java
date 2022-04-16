package hc.active;

import hc.enums.Severity;
import hc.utils.Timer;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

/**
 * Patient thread that moves through Rooms.<p>
 * Rooms store information about what is done in themselves and what the next step is.<p>
 * When next room does not exist Patient thread will exit.
 */
public class TPatient extends Thread implements IPatient {
    /**
     * Room Patient is currently in
     */
    private IContainer surroundings;
    private Severity severity=Severity.UNASSIGNED;
    private final Timer timer;
    private final boolean child;
    private int entranceNumber; //must persist over room number for disambiguation when leaving Entrance hall
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
        if (surroundings != null)
            surroundings.enter(this);
        while(surroundings != null && !Thread.interrupted())
            tryMove();
    }

    /**
     * Gets next room from their current environment<p>
     * This is blocking until the room thinks patient should leave<p>
     * Then moves between rooms<p>
     * After entering a new room patient warns previous room that they have left
     */
    private void tryMove(){
        IContainer next = surroundings.getFollowingContainer(this);// this blocks until the current container is done with you
        try {
            sleep(timer.getMovementTime());
            if (next != null)
                next.enter(this);
            this.surroundings.leave(this,next);
            this.surroundings = next;
        } catch (InterruptedException e) {
            surroundings = null;
            Thread.currentThread().interrupt();
        }



    }

    /**
     * Used at entrance hall to set values.<p>
     * Sets both room number and entrance number (used to pick which waiting room is released later on)<p>
     * Automatically updates UI/logs display value.
     * @param entranceNumber the assigned number
     */
    public void setEntranceNumber(int entranceNumber) {
        this.entranceNumber = entranceNumber;
        this.roomNumber = entranceNumber;
        this.displayValue = entranceNumber>=10?"":"0";
        this.displayValue+= String.valueOf(entranceNumber);
    }

    /**
     * Used at waiting hall to set room number.<p>
     * Automatically updates UI/logs display value.<p>
     * @param waitingNumber the assigned number
     */
    public void setWaitingNumber(int waitingNumber){
        this.displayValue = waitingNumber>=10?"":"0";
        this.displayValue+= String.valueOf(waitingNumber);
        this.roomNumber = waitingNumber;
    }

    /**
     * Used at payment hall to set room number.<p>
     * Automatically updates UI/logs display value<p>
     * @param paymentNumber the assigned number
     */
    public void setPaymentNumber(int paymentNumber){
        this.displayValue = paymentNumber>=10?"":"0";
        this.displayValue+= String.valueOf(paymentNumber);
        this.roomNumber = paymentNumber;
    }

    /**
     * Only used in Entrance Hall to decide whether to release an adult or a child based on lowest value.<p>
     * @return this patient's serial ID for Entrance Hall entry
     */
    @Override
    public int getEntranceNumber() {
    return entranceNumber;
    }

    /**
     * Used by Nurse entity after evaluation to set user status<p>
     * Used by Doctor entity to dismiss severity post-treatment<p>
     * @param severity user condition severity
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

    /**
     * Returns user identification string for logger and UI purposes.<p>
     * @return 3-4 character string including adulthood, numerical ID and severity.
     */
    public String getDisplayValue() {
        String prefix = this.child?"C":"A";
        String suffix = "";
        if (severity == Severity.BLUE)
            suffix = "B";
        if (severity == Severity.YELLOW)
            suffix = "Y";
        if (severity == Severity.RED)
            suffix = "R";
        return prefix+displayValue+suffix;
    }

    /**
     * Used in the majority of rooms to get user's entrance serial ID.<p>
     * Checked to assess whether a user is allowed to leave a room.<p>
     * This value can be set by the setter function or any other room value setter function.
     * @return room number
     */
    @Override
    public int getRoomNumber() {
        return roomNumber;
    }

    /**
     * Set a user's room priority, generally with a serial ID<p>
     * @param entered user priority in this given room
     */
    @Override
    public void setRoomNumber(int entered) {
        roomNumber = entered;

    }

}
