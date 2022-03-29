package hc.interfaces;

public interface ISeat {
    boolean canSeat(IPatient patient);
    boolean seat(IPatient patient);
    void leave();
}
