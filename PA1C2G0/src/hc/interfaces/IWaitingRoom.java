package hc.interfaces;

public interface IWaitingRoom extends IRoom{
    /**
     * Utilized by the container to tell a waiting room to release a patient
     */
    public void notifyDone();
    public IPatient[] getUsers();
}
