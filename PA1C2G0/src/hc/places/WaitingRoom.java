package hc.places;

import hc.MDelayFIFO;
import hc.interfaces.*;

/**
 * Class for rooms that hold several users
 */
public class WaitingRoom implements IWaitingRoom {
    private final IHall container;
    private final IContainer next;
    private final String name;
    private final MDelayFIFO<IPatient> patients;
    private String released; //so a given patient can be sure they were awakened
    //TODO: ASCERTAIN A BETTER SOLUTION THAN RELEASED (SPEED ISSUES), MAYBE A LIST?

    public WaitingRoom(IHall container, IContainer next, String name, int seats){
        this.container = container;
        this.next = next;
        this.name = name;
        patients = new MDelayFIFO(IPatient[].class, seats);
    }

    private boolean canEnter(IPatient patient) {
        return ! patients.isFull();
    }


    /**
     * Remove user -> at this point they have been notified and are leaving
     */
    @Override
    public void leave(IPatient patient) {
        patients.remove();
    }


    @Override
    public IContainer getFollowingContainer(IPatient patient) {

        while (released!= patient.getDisplayValue()) {
            try {
                patient.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return next;
    }

    @Override
    public void enter(IPatient tPatient) {
        patients.put(tPatient);
    }

    @Override
    public String getDisplayName() {
        return name;
    }


    @Override
    public String getState() {
        //TODO: SOMETHING, SHOULD PROBABLY REPORT THE MOST RECENT USER TO GET IN?
       return "";
    }

    @Override
    public void suspend() {

    }

    @Override
    public void resume() {

    }

    /**
     * Called by monitor only
     * Causes this room to tell the oldest user to get out
     */
    @Override
    public void notifyDone() {
        IPatient patient = patients.get(); //this notifies the oldest patient, causing them to leave getFollowingContainer
        released = patient.getDisplayValue();
        patient.notify();
    }

    @Override
    public IPatient[] getUsers(){
        //TODO: UI purpose code, probably develop a way to peek into FIFO
        return null;}
}
