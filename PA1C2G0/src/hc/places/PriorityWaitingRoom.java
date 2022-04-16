package hc.places;

import hc.queue.MDelayFIFO;
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
 * Implements severity band priority logic
 */
public class PriorityWaitingRoom implements IWaitingRoom {
    private final IWaitingHall container;
    private IContainer next;
    private final String name;
    private final MDelayFIFO<IPatient> patientsRed;
    private final MDelayFIFO<IPatient> patientsYellow;
    private final MDelayFIFO<IPatient> patientsBlue;
    private int releasedBlue = -1; // way to let a patient know if they've been released -> only affected while
                                   // inside hall lock
    private int releasedYellow = -1; // way to let a patient know if they've been released
    private int releasedRed = -1; // way to let a patient know if they've been released
    private final AtomicInteger entered = new AtomicInteger(0);
    private final int seats;
    private final ReentrantLock rl;
    private final Condition c;

    public PriorityWaitingRoom(IWaitingHall container, IContainer next, String name, int seats) {
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
     * Remove user
     * <p>
     * At this point the user has called enter() on their next container
     * <p>
     * This is called by the patient thread itself
     */
    @Override
    public void leave(IPatient patient, IContainer next) {
        MDelayFIFO queue = getPatientQueue(patient);
        queue.remove();
        container.notifyDone(this, patient);
    }

    /**
     * Called by patient thread
     * <p>
     * Gets next room only allowed through
     * 
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
            return releasedBlue;
        else if (severity.equals(Severity.YELLOW))
            return releasedYellow;
        else if (severity.equals(Severity.RED))
            return releasedRed;
        throw new RuntimeException("Unassigned patient in " + this.name);
    }

    private MDelayFIFO getPatientQueue(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (severity.equals(Severity.BLUE))
            return patientsBlue;
        else if (severity.equals(Severity.YELLOW))
            return patientsYellow;
        else if (severity.equals(Severity.RED))
            return patientsRed;
        throw new RuntimeException("Unassigned patient in " + this.name);
    }

    /**
     * Called by patient thread
     * <p>
     * Blocks on attempting to enter FIFO if full
     * 
     * @param tPatient patient thread
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
     * 
     * @return Map(room name, patientID[])
     */
    @Override
    public Map<String, String[]> getState() {
        HashMap<String, String[]> map = new HashMap();
        String[] patientText = new String[seats];
        int assigned = 0;
        for (IPatient[] patientList : new IPatient[][] { patientsRed.getSnapshot(seats),
                patientsYellow.getSnapshot(seats), patientsBlue.getSnapshot(seats) }) {
            for (int i = 0; i < seats - assigned; i++) {
                IPatient patient = patientList[i];
                if (patient == null)
                    break;
                patientText[assigned] = patient.getDisplayValue();
                assigned++;
            }
            if (assigned == seats)
                break;
        }

        map.put(this.name, patientText);
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
     * <p>
     * Causes this room to tell the oldest user to get out if any exists
     */
    @Override
    public void notifyDone() {
        try {
            rl.lock();
            IPatient patient = null;
            if (!patientsRed.isEmpty()) {
                patient = patientsRed.get();
                int rn = patient.getRoomNumber();
                releasedRed = releasedRed > rn ? releasedRed : rn;
            } else if (!patientsYellow.isEmpty()) {
                patient = patientsYellow.get();
                int rn = patient.getRoomNumber();
                releasedYellow = releasedYellow > rn ? releasedYellow : rn;
            } else if (!patientsBlue.isEmpty()) {
                patient = patientsBlue.get();
                int rn = patient.getRoomNumber();
                releasedBlue = releasedBlue > rn ? releasedBlue : rn;
            }
            if (patient != null)
                c.signalAll();
        } finally {
            rl.unlock();
        }
    }

    /**
     * Returns the next expected departure
     * 
     * @return patient, or <i>NULL</i> if empty
     */
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
     * 
     * @param next the container to set as following after this one
     */
    @Override
    public void setNext(IContainer next) {
        this.next = next;
    }
}
