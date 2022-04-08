package hc.interfaces;

/**
 * Interface for halls with "waiting" rooms
 * Notifies hall when a user enters waiting room in case they're allowed to instantly leave
 */
public interface IWaitingHall extends IHall{
    void notifyWaiting(IWaitingRoom room);
}
