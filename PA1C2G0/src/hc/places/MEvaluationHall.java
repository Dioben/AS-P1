package hc.places;

import hc.HCInstance;
import hc.enums.ReleasedRoom;
import hc.enums.Worker;
import hc.interfaces.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class MEvaluationHall implements IHall {

    private final  HCInstance instance;
    private final IWorkerRoom[] rooms;
    private final boolean[] available;
    private final ReentrantLock rl;
    private final String name = "EVH";
    private final ICallCenterWaiter callCenter;
    private final String nextHallName;

    /**
     * Instances an Evaluation Hall
     * @param instance space we are working inside of
     * @param after following container, a WTR is expected
     * @param callCenter entity that must be notified when someone leaves this space
     */
    public MEvaluationHall(HCInstance instance, IContainer after, ICallCenterWaiter callCenter){
        this.instance = instance;
        available = new boolean[]{true, true, true, true};
        IWorkerRoom evr1 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR1");
        IWorkerRoom evr2 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR2");
        IWorkerRoom evr3 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR3");
        IWorkerRoom evr4 = WorkerRoom.getRoom(Worker.NURSE,this,after,"EVR4");
        rooms = new IWorkerRoom[]{evr1,evr2,evr3,evr4};
        rl = new ReentrantLock();
        this.callCenter = callCenter;
        nextHallName = after.getDisplayName();
    }

    /**
     * Called by patient after they've managed to get to hallway's entrance<p>
     * Will direct patient to correct room<p>
     * If a patient has been allowed in here at all there IS an available room therefore there is no need to halt them
     * @param patient patient attempting to find next room
     * @return empty evaluation room
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
     * Gets current state of all rooms inside
     * @return a map linking room name to list of patients inside with length 1, may contain null
     */
    @Override
    public Map<String, String[]> getState() {
        HashMap<String,String[]> states = new HashMap<>();
        for (IWorkerRoom room : rooms)
            states.putAll(room.getState());
        return states;
    }

    /**
     * Pause all contained threads<p>
     * Due to patient pooling this only affects the contained nurses
     */
    @Override
    public void suspend() {
        for (IRoom room:rooms)
            room.suspend();

    }

    /**
     * Resume all contained threads<p>
     * Due to patient pooling this only affects the contained nurses
     */
    @Override
    public void resume() {
        for (IRoom room:rooms)
            room.resume();
    }

    /**
     * Propagates interrupt to contained nurses
     */
    @Override
    public void interrupt() {
        for (IRoom room:rooms)
            room.interrupt();
    }

    /**
     * Called by contained room to notify that patient is out<p>
     * Marks room as available again<p>
     * Signals Call center
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room,IPatient patient) {
        rl.lock();
        for (int i=0;i<rooms.length;i++){
            if (rooms[i]==room){
                available[i] = true;
                break;
            }
        }
        instance.notifyMovement(patient.getDisplayValue(),nextHallName);
        callCenter.notifyAvailable(ReleasedRoom.EVH);
        rl.unlock();

    }




    @Override
    public HCInstance getInstance() {
        return instance;
    }

    /**
     * Allow patient to enter this Hall<p>
     * No additional processing required
     * @param patient the patient attempting to enter the space
     */
    @Override
    public void enter(IPatient patient) {
    }

    /**
     * Notifies that a patient has left this hall and entered the evaluation rooms<p>
     * Room availability has already been changed to false, as such this method only needs to do logging
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient, IContainer next) {
        instance.notifyMovement(patient.getDisplayValue(),next.getDisplayName());
    }


}
