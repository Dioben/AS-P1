package hc.interfaces;

public interface ISeat {
    boolean canSeat(IPatient patient);
    void seat(IPatient patient);
}
