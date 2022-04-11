package hc.interfaces;

import java.util.Map;

/**
 * Generic Interfaces for spaces that a Patient can be inside
 * Fits both rooms and halls
 */
public interface IContainer extends ISeat{



    /**
     * Blocking call made by Patient
     * Patient tries to get access to next room
     * is not allowed until all required tasks are done
     * @param patient patient attempting to find next room
     * @return the container patient must move into next
     */
    IContainer getFollowingContainer(IPatient patient);


    /**
     * Get this container's name for logging and UI representation purposes
     * @return container's name
     */
    public String getDisplayName();


    /**
     * Current occupation status of container, used for Logger and UI
     * @return per room in container: "" if empty, otherwise user displayNumber + Band color
     */
    public Map<String,String[]> getState();

    /**
     * pause any threads involved with this container
     */
    public void suspend();

    /**
     * resume any threads involved with this container
     */
    public void resume();
}
