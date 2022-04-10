package hc.interfaces;

import hc.enums.Severity;

public interface IPatient extends Runnable{
    boolean isChild();
    void setEntranceNumber(int entranceNumber);
    void setWaitingNumber(int waitingNumber);
    void setPaymentNumber(int paymentNumber);
    void setSeverity(Severity severity);
    Severity getSeverity();
    String getDisplayValue();

    void suspend();
    void resume();

    int getRoomNumber();

    void setRoomNumber(int entered);

}
