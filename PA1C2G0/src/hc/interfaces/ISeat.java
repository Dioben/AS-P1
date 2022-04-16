package hc.interfaces;

/**
 * Interface for entities that a user can 'take a seat' in
 */
public interface ISeat {

    /**
     * Function that a patient must use to try to enter a given space
     * @param patient the patient attempting to enter the space
     */
    void enter(IPatient patient);

    /**
     * Notify given container that a patient has left it
     * @param patient Individual leaving space
     * @param next Space being vacated
     */
    void leave(IPatient patient, IContainer next);
}
