package hc.places;

import hc.active.TPatient;
import hc.interfaces.*;

public class WorkerRoom implements IRoom { //rooms for nurse,doctor,cashier: only supports one person at a time

    private IHall container;
    private IContainer next;
    private IPatient user;
    private IServiceWorker worker;
    @Override
    public boolean canEnter(TPatient patient) {
        return user==null;
    }

    @Override
    public boolean enter(TPatient patient) {//not supposed to ever actually return false
        if (canEnter(patient)){
            user = patient;
            return  true;
        }
        return false;
    }

    @Override
    public void leave() {
        user = null;
    }

    @Override
    public void notifyDone() {
        //TODO: FIGURE THIS OUT, PROBABLY HAS SOMETHING TO DO WITH CALL CENTER
    }

    @Override
    public IContainer getFollowingContainer() {
        worker.handleNextCostumer();
        return next;
    }

    @Override
    public void tryEnter(TPatient tPatient) {
    //TODO: FIGURE THIS OUT, PROBABLY HAS SOMETHING TO DO WITH CALL CENTER
    }
}
