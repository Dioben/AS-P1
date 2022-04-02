package hc.places;

import hc.HCInstance;
import hc.active.TPatient;
import hc.interfaces.IContainer;
import hc.interfaces.IHall;

public abstract class MHall implements IHall {
    @Override
    public boolean canEnter(TPatient patient) {
        //TODO: IMPLEMENT LOGIC FOR GENDERED ROOMS
        //TODO: IMPLEMENT
        return false;
    }

    @Override
    public boolean enter(TPatient patient) {
        return false;
    }

    @Override
    public void leave() {

    }

    @Override
    public void notifyDone() {

    }

    @Override
    public IContainer getFollowingContainer() {
        return null;
    }

    @Override
    public void tryEnter(TPatient tPatient) {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public HCInstance getInstance() {
        return null;
    }
}
