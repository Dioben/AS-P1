package hc.interfaces;

/**
 * Interface for entities that a user can take a seat in
 */
public interface ISeat {

    void tryEnter(IPatient patient);
    void leave(IPatient patient);
}
