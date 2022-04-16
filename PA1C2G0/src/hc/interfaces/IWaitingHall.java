package hc.interfaces;

/**
 * Interface for halls with "waiting" rooms
 * Notifies hall when a user enters waiting room in case they're allowed to instantly leave
 */
public interface IWaitingHall extends IHall{
    /**
     * Notify container that a user is now contained within this room, in case container wants to allow them to pass instantly
     * @param room room that has a new presence
     */
    void notifyWaiting(IWaitingRoom room);
}
