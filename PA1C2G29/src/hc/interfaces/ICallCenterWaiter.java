package hc.interfaces;

import hc.enums.ReleasedRoom;

/**
 * Used for classes that receive notifications from call center
 */
public interface ICallCenterWaiter {
    /**
     * Used by call center to inform of available rooms further ahead
     * 
     * @param releasedRoom the type of room released, used to fix ambiguity in
     *                     waiting and medical halls
     */
    void notifyAvailable(ReleasedRoom releasedRoom);
}
