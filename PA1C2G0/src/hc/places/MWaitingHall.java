package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.ReleasedRoom;
import hc.enums.Severity;
import hc.interfaces.*;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MWaitingHall implements IWaitingHall,ICallCenterWaiter {

    private final  HCInstance instance;
    private final ICallCenterWaiter callCenter; //must be set later due to object creation flow
    private final IWaitingRoom childRoom;
    private final IWaitingRoom adultRoom;
    private final String name = "WTH";
    private int inChild = 0;
    private int inAdult = 0;
    private final int roomMax;
    private final ReentrantLock rl;
    private final Condition childRoomAvailable;
    private final Condition adultRoomAvailable;
    private int entered = 0; //ID tracker

    private int releasedBlueAdult = -1; //helps patients know if they can leave
    private int releasedYellowAdult = -1; //helps patients know if they can leave
    private int releasedRedAdult = -1; //helps patients know if they can leave
    private int releasedBlueChild = -1; //helps patients know if they can leave
    private int releasedYellowChild = -1; //helps patients know if they can leave
    private int releasedRedChild = -1; //helps patients know if they can leave

    private final MFIFO<IPatient> childBacklogRed;
    private final MFIFO<IPatient> adultBacklogRed;
    private final MFIFO<IPatient> childBacklogYellow;
    private final MFIFO<IPatient> adultBacklogYellow;
    private final MFIFO<IPatient> childBacklogBlue;
    private final MFIFO<IPatient> adultBacklogBlue;
    private int nextSlackAdult; //we start out with 1 adult slots available in MDW
    private int nextSlackChild; //we start out with 1 child slots available in MDW

    public MWaitingHall(HCInstance instance, IContainer after, int seatsPerRoom, int adults, int children, int nextRoomSlackAdult, int nextRoomSlackChild, ICallCenterWaiter callCenter){
        this.instance = instance;
        childRoom = new PriorityWaitingRoom(this,after,"WTR2",seatsPerRoom);
        adultRoom = new PriorityWaitingRoom(this,after,"WTR1",seatsPerRoom);
        roomMax = seatsPerRoom;
        rl = new ReentrantLock();
        childRoomAvailable = rl.newCondition();
        adultRoomAvailable = rl.newCondition();
        childBacklogRed = new MFIFO(IPatient[].class,children);
        adultBacklogRed = new MFIFO(IPatient[].class,adults);
        childBacklogYellow = new MFIFO(IPatient[].class,children);
        adultBacklogYellow = new MFIFO(IPatient[].class,adults);
        childBacklogBlue = new MFIFO(IPatient[].class,children);
        adultBacklogBlue = new MFIFO(IPatient[].class,adults);
        nextSlackAdult = nextRoomSlackAdult;
        nextSlackChild = nextRoomSlackChild;
        this.callCenter = callCenter;
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
        MFIFO backlog = getBacklog(patient);
        if (inAdult==roomMax){
            backlog.put(patient);
            while (getControlNumber(patient)<patient.getRoomNumber()) {
                try {
                    adultRoomAvailable.await();
                } catch (InterruptedException e) {}
            }
        }

            inAdult++;
            rl.unlock();
        return adultRoom;

    }

    /**
     * Returns the appropriate release value based on patient severity
     * @param patient
     * @return WTN of the youngest released user in this severity group
     */
    private int getControlNumber(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (patient.isChild()){
            if (severity.equals(Severity.BLUE))
                return releasedBlueChild;
            if (severity.equals(Severity.RED))
                return releasedRedChild;
            if (severity.equals(Severity.YELLOW))
                return releasedYellowChild;
        }
        if (severity.equals(Severity.BLUE))
            return releasedBlueAdult;
        if (severity.equals(Severity.RED))
            return releasedRedAdult;
        if (severity.equals(Severity.YELLOW))
            return releasedYellowAdult;
        throw new RuntimeException("Unassigned patient in WTH");
    }

    /**
     * Returns the user's queue based on severity
     * @param patient
     * @return a backlog for user to wait in
     */
    private MFIFO getBacklog(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (severity.equals(Severity.UNASSIGNED))
            throw new RuntimeException("Unassigned patient in WTH");

        if (patient.isChild()){
            if (severity.equals(Severity.BLUE))
                return childBacklogBlue;
            if (severity.equals(Severity.RED))
                return childBacklogRed;
            if (severity.equals(Severity.YELLOW))
                return childBacklogYellow;
        }
        else{
            if (severity.equals(Severity.BLUE))
                return adultBacklogBlue;
            if (severity.equals(Severity.RED))
                return adultBacklogRed;
            if (severity.equals(Severity.YELLOW))
                return adultBacklogYellow;

        }
        return null;
    }

    /**
     * Called by patient
     * Move into child room as soon as it is available
     * @return this hall's child room
     * @param patient
     */
    private IContainer enterChildRoom(IPatient patient) {
        rl.lock();
        MFIFO backlog = getBacklog(patient);
        if (inChild==roomMax){
            backlog.put(patient);
            while(getControlNumber(patient)<patient.getRoomNumber()){
                try {
                    childRoomAvailable.await();
                } catch (InterruptedException e) {}
            }
        }

        inChild++;
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
    public Map<String, String[]> getState() {
        return null;
    }

    /**
     * Pause all contained threads
     * Due to patient pooling this method does not actually do anything
     */
    @Override
    public void suspend() {

    }

    /**
     * Resume all contained threads
     * Due to patient pooling this method does not actually do anything
     */
    @Override
    public void resume() {

    }

    /**
     * Called by contained room to notify that patient is out of WTR, calls CCH so that it can notify others
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room) {
        if (room==childRoom)
            callCenter.notifyAvailable(ReleasedRoom.WTR_CHILD);
        else if (room==adultRoom)
            callCenter.notifyAvailable(ReleasedRoom.WTR_ADULT);

    }

    /**
     * Identifies "oldest" child patient in queue if they exist and tells them to leave, updates containment state
     * Locked from parent method
     */
    private void handleChildRoomLeave() {
        inChild--;
        if(!childBacklogRed.isEmpty()) {
            IPatient patient = childBacklogRed.get();
            releasedRedChild = patient.getRoomNumber();
        }
        else if(!childBacklogYellow.isEmpty()){
            IPatient patient = childBacklogYellow.get();
            releasedYellowChild = patient.getRoomNumber();
        }else if(!childBacklogBlue.isEmpty()){
            IPatient patient = childBacklogBlue.get();
            releasedBlueChild = patient.getRoomNumber();
        }

        childRoomAvailable.signal();
    }

    /**
     * Identifies "oldest" adult patient in queue if they exist and tells them to leave, updates containment state
     * Locked from parent method
     */
    private void handleAdultRoomLeave() {
        inAdult--;
        if(!adultBacklogRed.isEmpty()) {
            IPatient patient = adultBacklogRed.get();
            releasedRedAdult = patient.getRoomNumber();
        }
        else if(!adultBacklogYellow.isEmpty()) {
            IPatient patient = adultBacklogYellow.get();
            releasedYellowAdult = patient.getRoomNumber();
        }
        else if(!adultBacklogBlue.isEmpty()) {
            IPatient patient = adultBacklogBlue.get();
            releasedBlueAdult = patient.getRoomNumber();
        }
        adultRoomAvailable.signal();


    }

    /**
     * Called by CCH to notify that some forward movement is expected
     * @param releasedRoom
     */
    @Override
    public void notifyAvailable(ReleasedRoom releasedRoom) {
        if (! (releasedRoom.equals(ReleasedRoom.WTR_ADULT)  || releasedRoom.equals(releasedRoom.WTR_CHILD) ||
                releasedRoom.equals(releasedRoom.MDW_ADULT) || releasedRoom.equals(releasedRoom.MDW_CHILD)) ){
            throw new RuntimeException("Waiting Hall was notified of the wrong movement: "+releasedRoom.name());
        }
        rl.lock();
        if (releasedRoom.equals(ReleasedRoom.MDW_ADULT)){
            if (inAdult==0){
                nextSlackAdult++;
            }
            else{
                adultRoom.notifyDone();
            }
        }
        else if (releasedRoom.equals(ReleasedRoom.MDW_CHILD)){
            if (inChild==0){
                nextSlackChild++;
            }
            else{
                childRoom.notifyDone();
            }
        } else if (releasedRoom.equals(ReleasedRoom.WTR_ADULT)){
            if (inAdult==0){
                rl.unlock();
                return;
            }
            handleAdultRoomLeave();
        }
        else if (releasedRoom.equals(ReleasedRoom.WTR_CHILD)){
            if (inChild==0){
                rl.unlock();
                return;
            }
            handleChildRoomLeave();

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
        patient.setWaitingNumber(entered);
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
     * Allow a waiting room patient to stop waiting without CCH call if we know there's space in MDW
     * @param room
     */
    @Override
    public void notifyWaiting(IWaitingRoom room) {
        rl.lock();
        if (room==childRoom && nextSlackChild>0){
            nextSlackChild--;
            room.notifyDone();
        }
        else if (room== adultRoom && nextSlackAdult>0){
            nextSlackAdult--;
            room.notifyDone();
        }

        rl.unlock();
    }
}
