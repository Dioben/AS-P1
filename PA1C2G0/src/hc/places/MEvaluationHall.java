package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.ReleasedRoom;
import hc.enums.Worker;
import hc.interfaces.*;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MEvaluationHall implements IHall {

    private final  HCInstance instance;
    private final IWorkerRoom[] rooms;
    private final boolean[] available;
    private final ReentrantLock rl;
    private final String name = "EVH";
    private final ICallCenterWaiter callCenter; //must be set later due to object creation flow

    public MEvaluationHall(HCInstance instance, IContainer after, ICallCenterWaiter callCenter){
        this.instance = instance;
        available = new boolean[]{false, false, false, false};
        IWorkerRoom evr1 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR1");
        IWorkerRoom evr2 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR2");
        IWorkerRoom evr3 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR3");
        IWorkerRoom evr4 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR4");
        rooms = new IWorkerRoom[]{evr1,evr2,evr3,evr4};
        rl = new ReentrantLock();
        this.callCenter = callCenter;
    }

    /**
     * Called by patient after they've managed to get to hallway's entrance
     * Will direct patient to correct room
     * If a patient has been allowed in here at all there IS an available room therefore there is no need to halt them
     * @param patient patient attempting to find next room
     * @return
     */
    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        IContainer value = null;
        rl.lock();
        for (int i =0;i<available.length;i++){
            if (available[i]){
                available[i] = false;
                value = rooms[i];
                break;
            }
        }
        rl.unlock();
        if (value == null){
            throw new RuntimeException("EVH did not have an available room");
        }
        return value;
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
     * Called by contained room to notify that patient is out, marks room as available again, should signal Call center
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room) {
        rl.lock();
        for (int i=0;i<rooms.length;i++){
            if (rooms[i]==room){
                available[i] = true;
                break;
            }
        }
        callCenter.notifyAvailable(ReleasedRoom.EVH);
        rl.unlock();

    }




    @Override
    public HCInstance getInstance() {
        return instance;
    }

    /**
     * Allow patient to enter this Hall
     * no additional processing required
     * @param patient the patient attempting to enter the space
     */
    @Override
    public void enter(IPatient patient) {
    }

    /**
     * Notifies that a patient has left this hall and entered the evaluation rooms
     * Room availability has already been changed to false, as such this method does not need to do anything
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient) {
    }


}
