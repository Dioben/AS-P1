package hc.places;

import hc.queue.MDelayFIFO;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for rooms that hold several users
 * Uses a single FIFO for removal logic
 */
public class WaitingRoom implements IWaitingRoom {
    private final IWaitingHall container;
    private IContainer next;
    private final String name;
    private final MDelayFIFO<IPatient> patients;
    private int released = -1;
    private AtomicInteger entered = new AtomicInteger(0);
    private final int seats;
    private final ReentrantLock rl;
    private final Condition cCanMove;

    public WaitingRoom(IWaitingHall container, IContainer next, String name, int seats) {
        this.container = container;
        this.next = next;
        this.name = name;
        patients = new MDelayFIFO(IPatient.class, seats);
        this.seats = seats;
        rl = new ReentrantLock();
        cCanMove = rl.newCondition();
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
        patients.remove();
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
            while (released < patient.getRoomNumber()) {
                try {
                    cCanMove.await();
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

    /**
     * Called by patient thread
     * <p>
     * Blocks on attempting to enter FIFO if full
     * 
     * @param tPatient patient thread
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

    /**
     * Maps all patients in this room by descending ID
     * 
     * @return Map(room name, patientID[])
     */
    @Override
    public Map<String, String[]> getState() {
        HashMap<String, String[]> map = new HashMap();
        IPatient[] patientList = patients.getSnapshot(seats);
        String[] patientText = new String[seats];

        for (int i = 0; i < seats; i++) {
            IPatient patient = patientList[i];
            if (patient == null)
                break;
            patientText[i] = patient.getDisplayValue();
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
     * Causes this room to tell the oldest user to get out
     */
    @Override
    public void notifyDone() {
        try {
            rl.lock();
            if (!patients.isEmpty()) {
                IPatient patient = patients.get(); // this notifies the oldest patient, causing them to leave
                                                   // getFollowingContainer
                int rn = patient.getRoomNumber();
                released = released > rn ? released : rn;
                cCanMove.signalAll();
            }

        } finally {
            rl.unlock();
        }
    }

    /**
     * Returns next expected departure
     * 
     * @return patient, or <i>NULL</i> if empty
     */

    @Override
    public IPatient getExpected() {
        return patients.getSnapshot(1)[0];
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
