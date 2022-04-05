package hc.interfaces;

import hc.HCInstance;

/**
 * Generic Interfaces for spaces that a Patient can be inside
 * Fits both rooms and halls
 */
public interface IContainer {
    /**
     * Check whether a room is empty and a patient qualifies to enter it
     * @param patient the subject entering a room
     * @return whether patient is allowed to enter
     */
    //TODO: WITH SEGREGATED QUEUES CHILD-SPECIFIC ROOMS/ THIS CONDITION MAY NOT EVEN BE REQUIRED -> this and enter shouldnt be here if tryEnter exists probably
    boolean canEnter(IPatient patient);

    /**
     * Makes patient enter a given container object
     * @param patient
     * @return whether enter was successful, expected to be true
     */
    boolean enter(IPatient patient);

    /**
     * Notify given container that a patient has left it
     * @param patient individual leaving space
     */
    void leave(IPatient patient);

    /**
     * Blocking call made by Patient
     * Patient tries to get access to next room
     * is not allowed until all required tasks are done
     * @param patient patient attempting to find next room
     * @return the container patient must move into next
     */
    IContainer getFollowingContainer(IPatient patient);

    /**
     * Blocking call that a patient must use to try to enter a given space
     * TODO: relies on canEnter/enter but those shouldnt be interface methods
     * @param patient the patient attempting to enter the space
     */
    void tryEnter(IPatient patient);

    /**
     * Get this container's name for logging and UI representation purposes
     * @return container's name
     */
    public String getDisplayName();

    /**
     * Get the instance this container is a part of
     * @return instance container belongs to
     */
    public HCInstance getInstance();

    /**
     * Current occupation status of container, used for Logger
     * @return "" if empty, otherwise user displayNumber + Band color
     */
    public String getState();

    /**
     * pause any threads involved with this container
     */
    public void suspend();

    /**
     * resume any threads involved with this container
     */
    public void resume();
}
