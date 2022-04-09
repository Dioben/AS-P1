package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.ReleasedRoom;
import hc.interfaces.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IWaitingHall,ICallCenterWaiter {

    private final  HCInstance instance;
    private final WaitingRoom childRoom;
    private final WaitingRoom adultRoom;
    private final String name = "ETH";
    private int inChild = 0;
    private int inAdult = 0;
    private final int roomMax;
    private final ReentrantLock rl;
    private final Condition childRoomAvailable;
    private final Condition adultRoomAvailable;
    private int entered = 0; //ID tracker
    private int released = 0; //helps patients know if they can leave
    private final MFIFO<IPatient> childBacklog;
    private final MFIFO<IPatient> adultBacklog;
    private int nextSlack; //we start out with 4 slots available in EVH
    private final MFIFO<Boolean> entrances; //stores entrance history, True if Child and False otherwise

    public MEntranceHall(HCInstance instance, IContainer after, int seatsPerRoom, int adults, int children, int nextRoomSlack){
        this.instance = instance;
        childRoom = new WaitingRoom(this,after,"ETR1",seatsPerRoom);
        adultRoom = new WaitingRoom(this,after,"ETR2",seatsPerRoom);
        roomMax = seatsPerRoom;
        rl = new ReentrantLock();
        childRoomAvailable = rl.newCondition();
        adultRoomAvailable = rl.newCondition();
        childBacklog = new MFIFO(IPatient[].class,children);
        adultBacklog = new MFIFO(IPatient[].class,adults);
        entrances = new MFIFO(Boolean[].class,seatsPerRoom*2);
        nextSlack = nextRoomSlack;
    }

    /**
     * Called by patient after they've managed to get to hallway's entrance
     * Will direct patient to correct room but then bar them from entering at all
     * @param patient patient attempting to find next room
     * @return
     */
    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        if (patient.isChild())
            return enterChildRoom(patient);
        return enterAdultRoom(patient);
    }

    /**Called by patient
     * Move into adult room as soon as it is available
     * @return this hall's adult room
     * @param patient
     */
    private IContainer enterAdultRoom(IPatient patient) {
        rl.lock();
        if (inAdult==roomMax){
            adultBacklog.put(patient);
            while (released<patient.getRoomNumber()) {
                try {
                    adultRoomAvailable.await();
                } catch (InterruptedException e) {}
            }
        }

            inAdult++;
            entrances.put(false);
            rl.unlock();
        return adultRoom;

    }

    /**
     * Called by patient
     * Move into child room as soon as it is available
     * @return this hall's child room
     * @param patient
     */
    private IContainer enterChildRoom(IPatient patient) {
        rl.lock();
        if (inChild==roomMax){
            childBacklog.put(patient);
            while(released<patient.getRoomNumber()){
                try {
                    childRoomAvailable.await();
                } catch (InterruptedException e) {}
            }
        }

        inChild++;
        entrances.put(true);
        rl.unlock();
        return childRoom;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * TODO: report the current state of this container for logging purposes
     * @return
     */
    @Override
    public String getState() {
        return null;
    }

    /** TODO
     * Pause all contained threads
     * With patient thread pooling this can be empty, otherwise we must propagate into room
     */
    @Override
    public void suspend() {

    }

    /** TODO
     * Resume all contained threads
     * With patient thread pooling this can be empty, otherwise we must propagate into room
     */
    @Override
    public void resume() {

    }

    /**
     * Called by contained room to notify that patient is out
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room) {
        rl.lock();
        if (inAdult==0 && inChild==0) {
            rl.unlock();
            return;
        }
        if (room==adultRoom)
            handleAdultRoomLeave();

        if (room==childRoom)
            handleChildRoomLeave();
        rl.unlock();

    }

    /**
     * Identifies "oldest" child patient in queue if they exist and tells them to leave, updates containment state
     */
    private void handleChildRoomLeave() {
        inChild--;
        if(!childBacklog.isEmpty()) {
            IPatient patient = childBacklog.get();
            released = patient.getRoomNumber();
        }
        childRoomAvailable.signal();
    }

    /**
     * Identifies "oldest" adult patient in queue if they exist and tells them to leave, updates containment state
     */
    private void handleAdultRoomLeave() {
        inAdult--;
        if(!adultBacklog.isEmpty()) {
            IPatient patient = adultBacklog.get();
            released = patient.getRoomNumber();
        }
        adultRoomAvailable.signal();


    }

    /**
     * Called by CCH to notify that some forward movement is expected
     * @param releasedRoom
     */
    @Override
    public void notifyAvailable(ReleasedRoom releasedRoom) {
        if (!releasedRoom.equals(ReleasedRoom.EVH)){
            throw new RuntimeException("Entrance Hall was notified of the wrong movement: "+releasedRoom.name());
        }
        rl.lock();
        if (inChild==0 && inAdult==0)
            nextSlack++;
        else{
            boolean wasChild = entrances.get();
            if (wasChild)
                childRoom.notifyDone();
            else
                adultRoom.notifyDone();
        }

        rl.unlock();
    }

    @Override
    public HCInstance getInstance() {
        return instance;
    }

    /**
     * Allow patient to enter this Hall
     * Automatically sets their room number and increments counter
     * @param patient the patient attempting to enter the space
     */
    @Override
    public void enter(IPatient patient) {
        rl.lock();
        patient.setEntranceNumber(entered);
        patient.setRoomNumber(entered);
        entered++;
        rl.unlock();
    }

    /**
     * Notifies that a patient has left this hall and entered the waiting rooms
     * As the counters were preemptively increased earlier and this class does not signal Call Center this function does nothing
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient) {
    }

    /**
     * Allow a waiting room patient to stop waiting without CCH call if we know there's space in EVH
     * @param room
     */
    @Override
    public void notifyWaiting(IWaitingRoom room) {
        rl.lock();
        if (nextSlack>0){
            nextSlack--;
            entrances.get(); //fast-forward entrance history
            room.notifyDone();
        }
        rl.unlock();
    }
}
