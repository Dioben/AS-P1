package hc.places;

import hc.interfaces.IContainer;
import hc.interfaces.IHall;
import hc.interfaces.IPatient;

public class ChildWorkerRoom extends WorkerRoom{
    protected ChildWorkerRoom(IHall container, IContainer next, String name) {
        super(container, next, name);
    }

    @Override
    public boolean canEnter(IPatient patient) {
        return getUser()==null && patient.isChild();
    }
}
