package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.ReleasedRoom;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEntranceHall implements IWaitingHall,ICallCenterWaiter {

    private final  HCInstance instance;
    private final IWaitingRoom childRoom;
    private final IWaitingRoom adultRoom;
    private final String name = "ETH";
    private int inChild = 0;
    private int inAdult = 0;
    private int assignedChild = 0;
    private int assignedAdult = 0;
    private final int roomMax;
    private final ReentrantLock rl;
    private final Condition childRoomAvailable;
    private final Condition adultRoomAvailable;
    private int entered = 0; //ID tracker
    private int releasedChild = -1; //helps patients know if they can leave
    private int releasedAdult = -1;
    private final IFIFO<IPatient> childBacklog;
    private final IFIFO<IPatient> adultBacklog;
    private int nextSlack; //we start out with 4 slots available in EVH
    private final int adults;
    private final int children;

    public MEntranceHall(HCInstance instance, IContainer after, int seatsPerRoom, int adults, int children, int nextRoomSlack){
        this.instance = instance;
        childRoom = new WaitingRoom(this,after,"ET2",seatsPerRoom);
        adultRoom = new WaitingRoom(this,after,"ET1",seatsPerRoom);
        roomMax = seatsPerRoom;
        rl = new ReentrantLock();
        childRoomAvailable = rl.newCondition();
        adultRoomAvailable = rl.newCondition();
        childBacklog = new MFIFO(IPatient.class,children);
        adultBacklog = new MFIFO(IPatient.class,adults);
        nextSlack = nextRoomSlack;
        this.adults = adults;
        this.children = children;
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
        if (assignedAdult==roomMax || !adultBacklog.isEmpty()){
            adultBacklog.put(patient);
            while (releasedAdult<patient.getRoomNumber()) {
                try {
                    adultRoomAvailable.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

            assignedAdult++;
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
        if (assignedChild==roomMax || !childBacklog.isEmpty()){
            childBacklog.put(patient);
            while(releasedChild<patient.getRoomNumber()){
                try {
                    childRoomAvailable.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        assignedChild++;
        rl.unlock();
        return childRoom;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * @return Empty String if no patient exists in this room, otherwise the display value of latest patient to enter
     */
    @Override
    public Map<String, String[]> getState() {
        HashMap<String,String[]> vals = new HashMap<>();

        IPatient[] adults = adultBacklog.getSnapshot(this.adults);
        IPatient[] children = childBacklog.getSnapshot(this.children);
        IPatient[][] patients = new IPatient[][] {
                adults, children
        };
        String[] adultsState = new String[adults.length];
        String[] childrenState = new String[children.length];
        String[][] patientsState = new String[][] {
                adultsState, childrenState
        };

        for (int i = 0; i < patients.length; i++) {
            for (int j = 0; j < patients[i].length; j++) {
                IPatient patient = patients[i][j];
                if (patient == null)
                    break;
                patientsState[i][j] = patient.getDisplayValue();
            }
        }

        vals.put(this.name + "A",adultsState);
        vals.put(this.name + "C",childrenState);
        vals.putAll(childRoom.getState());
        vals.putAll(adultRoom.getState());
        return vals;
    }

    /**
     * Due to patient thread pooling this function can be empty, if we had any workers we'd have to pause them
     */
    @Override
    public void suspend() {

    }

    /**
     * Due to patient thread pooling this function can be empty, if we had any workers we'd have to pause them
     */
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
     * Called by contained room to notify that patient is out
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room, IPatient patient) {
        rl.lock();
        if (room==adultRoom)
            handleAdultRoomLeave();

        if (room==childRoom)
            handleChildRoomLeave();

        instance.notifyMovement(patient.getDisplayValue(),null);
        rl.unlock();

    }

    /**
     * Identifies "oldest" child patient in queue if they exist and tells them to leave, updates containment state
     */
    private void handleChildRoomLeave() {
        inChild--;
        assignedChild--;
        if(!childBacklog.isEmpty()) {
            IPatient patient = childBacklog.get();
            releasedChild = patient.getRoomNumber();
        }
        childRoomAvailable.signalAll();
    }

    /**
     * Identifies "oldest" adult patient in queue if they exist and tells them to leave, updates containment state
     */
    private void handleAdultRoomLeave() {
        assignedAdult--;
        inAdult--;
        if(!adultBacklog.isEmpty()) {
            IPatient patient = adultBacklog.get();
            releasedAdult = patient.getRoomNumber();
        }
        adultRoomAvailable.signalAll();
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
        if (inChild==0 && inAdult==0){
            nextSlack++;
            rl.unlock();
        }
        else if (inChild==0) {
                rl.unlock();
                adultRoom.notifyDone();
        }
        else if (inAdult==0){
            rl.unlock();
            childRoom.notifyDone();
        }
        else{
            IPatient nextAdult = adultRoom.getExpected();
            IPatient nextChild = childRoom.getExpected();
            if (nextAdult==null && nextChild==null)
                throw new RuntimeException("Found NULL inside a waiting room");
            rl.unlock();
            if (nextAdult.getEntranceNumber()<nextChild.getEntranceNumber())
                adultRoom.notifyDone();
            else
                childRoom.notifyDone();
            }
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
        instance.notifyMovement(patient.getDisplayValue(),this.name);
        rl.unlock();
    }

    /**
     * Notifies that a patient has left this hall and entered the waiting rooms
     * As the counters were preemptively increased earlier and this class does not signal Call Center this function only notifies instance to log changes
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient,IContainer next) {
        instance.notifyMovement(patient.getDisplayValue(),next.getDisplayName());
    }

    /**
     * Allow a waiting room patient to stop waiting without CCH call if we know there's space in WTR
     * @param room
     */
    @Override
    public void notifyWaiting(IWaitingRoom room) {
        rl.lock();
        if (room==childRoom)
            inChild++;
        if (room==adultRoom)
            inAdult++;
        if (nextSlack>0){
            nextSlack--;
            rl.unlock();
            room.notifyDone();
        }else
            rl.unlock();
    }
}
