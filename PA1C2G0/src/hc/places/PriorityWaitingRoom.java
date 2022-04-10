package hc.places;

import hc.MDelayFIFO;
import hc.MFIFO;
import hc.enums.Severity;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;
import hc.interfaces.IWaitingHall;
import hc.interfaces.IWaitingRoom;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for rooms that hold several users
 */
public class PriorityWaitingRoom implements IWaitingRoom {
    private final IWaitingHall container;
    private IContainer next;
    private final String name;
    private final MDelayFIFO<IPatient> patientsRed;
    private final MDelayFIFO<IPatient> patientsYellow;
    private final MDelayFIFO<IPatient> patientsBlue;
    private int releasedBlue = -1; //way to let a patient know if they've been released -> only affected while inside hall lock
    private int releasedYellow = -1; //way to let a patient know if they've been released
    private int releasedRed = -1; //way to let a patient know if they've been released
    private AtomicInteger entered =  new AtomicInteger(0);

    public PriorityWaitingRoom(IWaitingHall container, IContainer next, String name, int seats){
        this.container = container;
        this.next = next;
        this.name = name;
        patientsRed = new MDelayFIFO(IPatient[].class, seats);
        patientsYellow = new MDelayFIFO(IPatient[].class, seats);
        patientsBlue = new MDelayFIFO(IPatient[].class, seats);
    }

    private boolean canEnter(IPatient patient) {
        Severity severity = patient.getSeverity();
        MDelayFIFO queue = getPatientQueue(patient);
        return  queue.isFull();
    }


    /**
     * Remove user -> at this point they have been notified and are leaving
     * This is called by the patient thread itself
     */
    @Override
    public void leave(IPatient patient) {
        MDelayFIFO queue = getPatientQueue(patient);
        queue.remove();
        container.notifyDone(this);
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
        while (getControlNumber(patient) < patient.getRoomNumber()) {
            try {
                patient.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return next;
    }

    private int getControlNumber(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (severity.equals(Severity.BLUE))
            return  releasedBlue;
        else if (severity.equals(Severity.YELLOW))
            return releasedYellow;
        else if (severity.equals(Severity.RED))
            return releasedRed;
        throw  new RuntimeException("Unassigned patient in "+this.name);
    }

    private MDelayFIFO getPatientQueue(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (severity.equals(Severity.BLUE))
            return  patientsBlue;
        else if (severity.equals(Severity.YELLOW))
            return patientsYellow;
        else if (severity.equals(Severity.RED))
            return patientsRed;
        throw  new RuntimeException("Unassigned patient in "+this.name);
    }

    /**
     * Called by patient thread
     * Blocks on attempting to enter FIFO
     *
     * @param tPatient
     */
    @Override
    public void enter(IPatient tPatient) {
        MDelayFIFO queue = getPatientQueue(tPatient);
        queue.put(tPatient);
        tPatient.setRoomNumber(entered.getAndIncrement());
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
     * Called from monitor only
     * Causes this room to tell the oldest user to get out
     *
     */
    @Override
    public void notifyDone() {
        IPatient patient=null;
        if(!patientsRed.isEmpty()) {
            patient = patientsRed.get();
            releasedRed = patient.getRoomNumber();
        }
        else if(!patientsYellow.isEmpty()){
            patient = patientsYellow.get();
            releasedYellow = patient.getRoomNumber();
        }else if(!patientsBlue.isEmpty()){
            patient = patientsBlue.get();
            releasedBlue = patient.getRoomNumber();
        }
        if (patient!=null)
            patient.notify();
    }

    @Override
    public IPatient[] getUsers(){
        //TODO: UI purpose code, probably develop a way to peek into FIFO
        return null;}

    /**
     * Overrides the next field in cases where it might be ambiguous
     * @param next The container to set as following after this one
     */
    @Override
    public void setNext(IContainer next) {
        this.next = next;
    }
}
