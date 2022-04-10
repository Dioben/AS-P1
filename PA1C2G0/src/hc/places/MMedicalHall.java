package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.ReleasedRoom;
import hc.enums.Worker;
import hc.interfaces.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MMedicalHall implements IWaitingHall,ICallCenterWaiter {

    private final  HCInstance instance;
    private final IWaitingRoom childWaitingRoom;
    private final IWaitingRoom adultWaitingRoom;
    private final IWorkerRoom childWorkerRoom1;
    private final IWorkerRoom adultWorkerRoom1;
    private final IWorkerRoom childWorkerRoom2;
    private final IWorkerRoom adultWorkerRoom2;
    private final String name = "MDH";
    private final int roomMax = 1;
    private final ReentrantLock rl;
    private int entered = 0; //ID tracker
    private int releasedChild = -1; //helps patients know if they can leave
    private int releasedAdult = -1;
    private int nextSlackAdult = 2;
    private int nextSlackChild = 2;
    private boolean inAdult = false;
    private boolean inChild = false;
    private final ICallCenterWaiter callCenter;

    public MMedicalHall(HCInstance instance, IContainer after, int adults, int children, ICallCenterWaiter callCenter){
        this.instance = instance;
        childWorkerRoom1 = WorkerRoom.getRoom(Worker.DOCTOR,this,after,"MDR1");
        childWorkerRoom2 = WorkerRoom.getRoom(Worker.DOCTOR,this,after,"MDR2");
        adultWorkerRoom1 = WorkerRoom.getRoom(Worker.DOCTOR,this,after,"MDR3");
        adultWorkerRoom2 = WorkerRoom.getRoom(Worker.DOCTOR,this,after,"MDR4");

        childWaitingRoom = new WaitingRoom(this,childWorkerRoom1,"MDW1",roomMax);
        adultWaitingRoom = new WaitingRoom(this,adultWorkerRoom1,"MDW2",roomMax);
        rl = new ReentrantLock();
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
        if (patient.isChild()) {
            inChild = true;
            return childWaitingRoom;
        }
        inAdult = true;
        return adultWaitingRoom;
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
     * Can be called by waiting rooms to notify that patient is no longer waiting or by doctor to notify that patient is done
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room) {
        rl.lock();
        if (room==childWaitingRoom){
            callCenter.notifyAvailable(ReleasedRoom.MDW_CHILD);
        }
        if (room==adultWaitingRoom){
            callCenter.notifyAvailable(ReleasedRoom.MDW_ADULT);
        }
        if (room==adultWorkerRoom1 || room==adultWorkerRoom2)
            callCenter.notifyAvailable(ReleasedRoom.MDR_ADULT);
        if (room==childWorkerRoom1 || room==childWorkerRoom2)
            callCenter.notifyAvailable(ReleasedRoom.MDR_CHILD);
        rl.unlock();

    }

    /**
     * Called by CCH to notify that some forward movement is expected
     * Will notify a patient in the correct room to start moving again
     * @param releasedRoom
     */
    @Override
    public void notifyAvailable(ReleasedRoom releasedRoom) {
        if (! (releasedRoom.equals(ReleasedRoom.MDR_CHILD) || releasedRoom.equals(ReleasedRoom.MDR_ADULT))){
            throw new RuntimeException("Medical Hall was notified of the wrong movement: "+releasedRoom.name());
        }
        rl.lock();
        if (releasedRoom==ReleasedRoom.MDR_ADULT){
            if (inAdult){
                inAdult = false;
                adultWaitingRoom.setNext(getFreeAdultRoom());
                adultWaitingRoom.notifyDone();
            }else{
                nextSlackAdult++;
            }
        }
        if (releasedRoom==ReleasedRoom.MDR_CHILD){
            if (inChild){
                inChild = false;
                childWaitingRoom.setNext(getFreeChildRoom());
                childWaitingRoom.notifyDone();
            }else{
                nextSlackChild++;
            }
        }

        rl.unlock();
    }

    private IContainer getFreeAdultRoom() {
        if (adultWorkerRoom1.canEnter())
            return adultWorkerRoom1;
        else if (adultWorkerRoom2.canEnter())
            return adultWorkerRoom2;
        throw new RuntimeException("Moved waiting user with no free doctor");
    }

    private IContainer getFreeChildRoom() {
        if (childWorkerRoom1.canEnter())
            return childWorkerRoom1;
        else if (childWorkerRoom2.canEnter())
            return childWorkerRoom2;
        throw new RuntimeException("Moved waiting user with no free doctor");
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
     * Allow a waiting room patient to stop waiting without CCH call if we know there's space in MDR
     * @param room
     */
    @Override
    public void notifyWaiting(IWaitingRoom room) {
        rl.lock();
        if (room==childWaitingRoom){
            if (nextSlackChild>0){
                nextSlackChild--;
                room.setNext(getFreeChildRoom());
                room.notifyDone();
            }

        }
        else if (room==adultWaitingRoom){
            if (nextSlackAdult>0){
                nextSlackAdult--;
                room.setNext(getFreeAdultRoom());
                room.notifyDone();
            }
        }
        rl.unlock();
    }
}
