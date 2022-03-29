package hc.places;

import hc.active.TPatient;
import hc.interfaces.IContainer;
import hc.interfaces.IHall;

public class AdultWorkerRoom extends WorkerRoom{
    protected AdultWorkerRoom(IHall container, IContainer next, String name) {
        super(container, next, name);
    }

    @Override
    public boolean canEnter(TPatient patient) {
        return getUser()==null && !patient.isChild();
    }
}
