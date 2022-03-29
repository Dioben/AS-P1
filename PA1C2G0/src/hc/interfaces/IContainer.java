package hc.interfaces;

import hc.active.TPatient;

public interface IContainer {
    boolean canEnter(TPatient patient); //check whether is empty and patient qualifies
    void enter(TPatient patient); //actually enter container object
    void leave(); //free up container

    void notifyDone(); // tells container that room processing is done
    IContainer getFollowingContainer(); //provides next space entity must move into AFTER room processing is done
    void tryEnter(TPatient tPatient); //blocking call that waits for notif to allow patient to enter
}
