package hc.places;

import hc.interfaces.IContainer;
import hc.interfaces.IHall;
import hc.interfaces.IPatient;

/**
 * Worker Room subclass that can only contain adults, used for adult-only doctor rooms
 * TODO: possibly remove
 */
public class AdultWorkerRoom extends WorkerRoom{
    protected AdultWorkerRoom(IHall container, IContainer next, String name) {
        super(container, next, name);
    }

    @Override
    public boolean canEnter(IPatient patient) {
        return getUser()==null && !patient.isChild();
    }
}
