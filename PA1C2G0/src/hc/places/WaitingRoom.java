package hc.places;

import hc.MDelayFIFO;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for rooms that hold several users
 */
public class WaitingRoom implements IWaitingRoom {
    private final IWaitingHall container;
    private IContainer next;
    private final String name;
    private final MDelayFIFO<IPatient> patients;
    private int released = -1; //way to let a patient know if they've been released -> only ever changed by 1 thread
    private AtomicInteger entered =  new AtomicInteger(0);
    private final int seats;

    public WaitingRoom(IWaitingHall container, IContainer next, String name, int seats){
        this.container = container;
        this.next = next;
        this.name = name;
        patients = new MDelayFIFO(IPatient[].class, seats);
        this.seats = seats;
    }


    /**
     * Remove user -> at this point they have been notified and are leaving
     * This is called by the patient thread itself
     */
    @Override
    public void leave(IPatient patient) {
        patients.remove();
        container.notifyDone(this,patient);
    }

    /**
     * called by patient thread
     * uses non-sync variable released but only reads it
     * @param patient patient attempting to find next room
     * @return the room patient must move into next
     */
    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        container.notifyWaiting(this);
        while (released <= patient.getRoomNumber()) {
            try {
                patient.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return next;
    }

    /**
     * Called by patient thread
     * Blocks on attempting to enter FIFO
     *
     * @param tPatient
     */
    @Override
    public void enter(IPatient tPatient) {
        patients.put(tPatient);
        tPatient.setRoomNumber(entered.getAndIncrement());
    }

    @Override
    public String getDisplayName() {
        return name;
    }


    @Override
    public Map<String, String[]> getState() {
        HashMap<String,String[]> map = new HashMap();
        IPatient[] patientList = patients.getSnapshot(seats);
        String[] patientText = new String[seats];

        for(int i=0;i<seats;i++){
            IPatient patient = patientList[i];
            if (patient==null)
                break;
            patientText[i] = patient.getDisplayValue();
        }

        map.put(this.name,patientText);
       return map;
    }

    @Override
    public void suspend() {

    }

    @Override
    public void resume() {

    }

    /**
     * Called from monitor only
     * Causes this room to tell the oldest user to get out
     *
     */
    @Override
    public void notifyDone() {
        IPatient patient = patients.get(); //this notifies the oldest patient, causing them to leave getFollowingContainer
        released = patient.getRoomNumber();
        patient.notify();
    }


    /**
     * Overrides the next field in cases where it might be ambiguous
     * @param next The container to set as following after this one
     */
    @Override
    public void setNext(IContainer next) {
    this.next = next;
    }
}
