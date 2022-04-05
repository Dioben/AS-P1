package hc.interfaces;

/**
 * Interface for entities that a user can take a seat in
 * TODO: SEE CONTAINER "REMOVE-CAUSE-OF-TRY-ENTER" DEBACLE, maybe make container implement this
 */
public interface ISeat {
    boolean canEnter(IPatient patient);
    boolean enter(IPatient patient);
    void leave(IPatient patient);
}
