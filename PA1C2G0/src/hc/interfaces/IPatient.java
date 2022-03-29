package hc.interfaces;

import hc.Severity;

public interface IPatient {
    boolean isChild();
    public void setEntranceNumber(int entranceNumber);
    public void setWaitingNumber(int waitingNumber);
    public void setPaymentNumber(int paymentNumber);
    public void setSeverity(Severity severity);
}
