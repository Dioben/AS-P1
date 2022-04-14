package hc.places;

import hc.MDelayFIFO;
import hc.enums.Severity;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;
import hc.interfaces.IWaitingHall;
import hc.interfaces.IWaitingRoom;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for rooms that hold several users
 * This room can overflow if used on their own, container object must implement limiting logic
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
    private final AtomicInteger entered =  new AtomicInteger(0);
    private final int seats;
    private final ReentrantLock rl;
    private final Condition c;

    public PriorityWaitingRoom(IWaitingHall container, IContainer next, String name, int seats){
        this.container = container;
        this.next = next;
        this.name = name;
        patientsRed = new MDelayFIFO(IPatient.class, seats);
        patientsYellow = new MDelayFIFO(IPatient.class, seats);
        patientsBlue = new MDelayFIFO(IPatient.class, seats);
        this.seats = seats;
        rl = new ReentrantLock();
        c = rl.newCondition();
    }



    /**
     * Remove user -> at this point they have been notified and are leaving
     * This is called by the patient thread itself
     */
    @Override
    public void leave(IPatient patient,IContainer next) {
        MDelayFIFO queue = getPatientQueue(patient);
        queue.remove();
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
        try {
            rl.lock();
            container.notifyWaiting(this);
            while (getControlNumber(patient) < patient.getRoomNumber()) {
                try {
                    c.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        } finally {
            rl.unlock();
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


    /**
     * Maps all patients in this room by descending severity and ID
     * @return Map<room name, patient string list>
     */
    @Override
    public Map<String, String[]> getState() {
        HashMap<String,String[]> map = new HashMap();
        String[] patientText = new String[seats];
        int assigned = 0;
        for (IPatient[] patientList: new IPatient[][]{patientsRed.getSnapshot(seats),patientsYellow.getSnapshot(seats),patientsBlue.getSnapshot(seats)}) {
            for (int i = 0; i < seats - assigned; i++) {
                IPatient patient = patientList[i];
                if (patient == null)
                    break;
                patientText[assigned] = patient.getDisplayValue();
                assigned++;
            }
            if (assigned==seats)
                break;
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
     * Due to patient thread pooling there is no need to interrupt patients here
     */
    @Override
    public void interrupt() {

    }

    /**
     * Called from monitor only
     * Causes this room to tell the oldest user to get out
     *
     */
    @Override
    public void notifyDone() {
        try {
            rl.lock();
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
                c.signalAll();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public IPatient getExpected() {
        rl.lock();
        IPatient patient = null;
        if (!patientsRed.isEmpty())
            patient = patientsRed.getSnapshot(1)[0];
        else if (!patientsYellow.isEmpty())
            patient = patientsYellow.getSnapshot(1)[0];
        else if (!patientsBlue.isEmpty())
            patient = patientsBlue.getSnapshot(1)[0];
        rl.unlock();
        return patient;
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
