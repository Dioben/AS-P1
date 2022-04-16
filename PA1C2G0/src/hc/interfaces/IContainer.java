package hc.interfaces;

import java.util.Map;

/**
 * Generic Interfaces for spaces that a Patient can be inside
 * Fits both rooms and halls
 */
public interface IContainer extends ISeat{

    /**
     * Blocking call made by Patient<p>
     * Patient tries to get access to next room<p>
     * is not allowed until all required tasks are done.
     * @param patient patient attempting to find next room
     * @return The container patient must move into next, <i>NULL</i> when at the end of the line
     */
    IContainer getFollowingContainer(IPatient patient);


    /**
     * Get this container's name for logging and UI representation purposes.
     * @return Container's name
     */
    String getDisplayName();


    /**
     * Current occupation status of container, used for Logger and UI.
     * @return per room in container: mapping of room name to patient list, list size may vary and be null padded.
     */
    Map<String,String[]> getState();

    /**
     * pause any threads involved with this container
     */
    void suspend();

    /**
     * resume any threads involved with this container
     */
    void resume();

    /**
     * Interrupt any activity inside this container<p>
     * Intended use is clean shutdown
     */
    void interrupt();
}
