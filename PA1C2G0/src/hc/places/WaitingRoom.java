package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.active.*;
import hc.enums.Worker;
import hc.interfaces.*;

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

    @Override
    public boolean canEnter(IPatient patient) {
        return ! patients.isFull();
    }

    @Override
    public boolean enter(IPatient patient) {
        patients.put(patient);
        return true;
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
    public void tryEnter(IPatient tPatient) {
        //TODO: FIGURE THIS OUT, PROBABLY HAS SOMETHING TO DO WITH CALL CENTER
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public HCInstance getInstance() {
        return container.getInstance();
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
