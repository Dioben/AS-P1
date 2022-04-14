package hc.interfaces;

import hc.enums.Severity;

public interface IPatient extends Runnable{
    boolean isChild();
    void setEntranceNumber(int entranceNumber);
    void setWaitingNumber(int waitingNumber);
    void setPaymentNumber(int paymentNumber);

    int getEntranceNumber();
    int getWaitingNumber();
    int getPaymentNumber();

    void setSeverity(Severity severity);
    Severity getSeverity();
    String getDisplayValue();

    /**
     * Thread class methods
     */
    void suspend();
    void resume();
    boolean isAlive();


    int getRoomNumber();
    void setRoomNumber(int entered);

}
