package hc.places;

import hc.HCInstance;
import hc.interfaces.IContainer;
import hc.interfaces.IHall;
import hc.interfaces.IPatient;
import hc.interfaces.IRoom;

public abstract class MHall implements IHall {
    @Override
    public boolean canEnter(IPatient patient) {
        //TODO: IMPLEMENT LOGIC FOR GENDERED ROOMS
        //TODO: IMPLEMENT
        return false;
    }

    @Override
    public boolean enter(IPatient patient) {
        return false;
    }

    @Override
    public void leave(IPatient patient) {

    }

    @Override
    public void notifyDone(IRoom room) {

    }

    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        return null;
    }

    @Override
    public void tryEnter(IPatient patient) {

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