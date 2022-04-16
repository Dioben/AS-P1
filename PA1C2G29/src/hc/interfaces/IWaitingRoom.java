package hc.interfaces;

public interface IWaitingRoom extends IRoom {
    /**
     * Utilized by the container to tell a waiting room to release a patient
     */
    public void notifyDone();

    /**
     * Returns the next leaving patient in this room
     * <p>
     * Used to evaluate which room to release in EntranceHall
     * 
     * @return next patient to leave or <i>NULL</i>
     */
    public IPatient getExpected();
}
