package hc.interfaces;

import hc.enums.Severity;

public interface IPatient extends Runnable{
    boolean isChild();
    void setEntranceNumber(int entranceNumber);
    void setWaitingNumber(int waitingNumber);
    void setPaymentNumber(int paymentNumber);

    int getEntranceNumber();

    void setSeverity(Severity severity);
    Severity getSeverity();
    String getDisplayValue();

    /**
     * Used to get current room priority
     * @return <b>int</b> representing room priority
     */
    int getRoomNumber();

    /**
     * Set room priority<p>
     * SetEntrance/Payment/WaitingNumber implicitly do this
     * @param entered the room's priority number
     */
    void setRoomNumber(int entered);

    //Thread class methods
    void suspend();
    void resume();
    void interrupt();
    boolean isAlive();
}
