package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.interfaces.*;

/**
 * Class for rooms that hold several users
 */
public class WaitingRoom implements IRoom {
    private final IHall container;
    private final IContainer next;
    private final String name;
    private MFIFO<IPatient> patients;

    public WaitingRoom(IHall container, IContainer next, String name, int seats){
        this.container = container;
        this.next = next;
        this.name = name;
    }

    private boolean canEnter(IPatient patient) {
        return ! patients.isFull();
    }


    /**
     * Remove oldest user -> problem: user is who calls this and he's delayed
     */
    @Override
    public void leave(IPatient patient) {
        patients.get(); //waiting rooms follow FIFO logic
    }


    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        return next;
    }

    @Override
    public void enter(IPatient tPatient) {
        //TODO: FIGURE THIS OUT, PROBABLY HAS SOMETHING TO DO WITH CALL CENTER
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

    public IPatient getUser(){
        //TODO: UNSURE, THIS IS SUPPOSED TO BE USED FOR UI PURPOSES AND DOESNT REALLY MATCH DEFINED INTERFACE
        return null;}
}
