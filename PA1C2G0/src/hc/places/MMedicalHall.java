package hc.places;

import hc.HCInstance;
import hc.enums.ReleasedRoom;
import hc.enums.Worker;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;
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

    public MMedicalHall(HCInstance instance, IContainer after, ICallCenterWaiter callCenter){
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
        rl.lock();
        if (patient.isChild()) {
            rl.unlock();
            return childWaitingRoom;
        }
        rl.unlock();
        return adultWaitingRoom;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * Obtain a map linking each container to patient display name array
     * Used for UI purposes
     * @return Map<Container Name, Patient Names>
     */
    @Override
    public Map<String, String[]> getState() {
        Map<String,String[]> states = new HashMap<>();
        String[] inWaiting = new String[2];
        int idx = 0;
        inWaiting[idx] = childWaitingRoom.getState().get(childWaitingRoom.getDisplayName())[0];
        if (inWaiting[idx]!=null)
            idx++;
        inWaiting[idx] = adultWaitingRoom.getState().get(adultWaitingRoom.getDisplayName())[0];
        states.put(this.name,inWaiting);
        states.putAll(adultWaitingRoom.getState());
        states.putAll(childWorkerRoom1.getState());
        states.putAll(childWorkerRoom2.getState());
        states.putAll(adultWorkerRoom1.getState());
        states.putAll(adultWorkerRoom2.getState());


        return states;
    }

    /**
     * Pause all contained threads
     * Due to patient pooling this only stops the container doctors
     */
    @Override
    public void suspend() {
        childWorkerRoom1.suspend();
        childWorkerRoom2.suspend();
        adultWorkerRoom1.suspend();
        adultWorkerRoom2.suspend();

    }

    /**
     * Resume all contained threads
     * Due to patient pooling this only stops the container doctors
     */
    @Override
    public void resume() {
        childWorkerRoom1.resume();
        childWorkerRoom2.resume();
        adultWorkerRoom1.resume();
        adultWorkerRoom2.resume();
    }

    /**
     * Called by contained room to notify that patient is out
     * Can be called by waiting rooms to notify that patient is no longer waiting or by doctor to notify that patient is done
     * Calls logger, call center, updates UI thread
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room, IPatient patient) {
        rl.lock();
        if (room==childWaitingRoom){
            //figure out which room this patient actually moved on to, then notify movement
            String patientName = patient.getDisplayValue();
            String childPatient1name = childWorkerRoom1.getState().get(childWorkerRoom1.getDisplayName())[0];
            if (childPatient1name.equals(patientName))
                instance.notifyMovement(patientName,childWorkerRoom1.getDisplayName());
            else{
                instance.notifyMovement(patientName,childWorkerRoom2.getDisplayName());
            }
            callCenter.notifyAvailable(ReleasedRoom.MDW_CHILD);

        }
        else if (room==adultWaitingRoom){
            //figure out which room this patient actually moved on to, then notify movement
            String patientName = patient.getDisplayValue();
            String adultPatient1name = adultWorkerRoom1.getState().get(adultWorkerRoom1.getDisplayName())[0];
            if (adultPatient1name.equals(patientName))
                instance.notifyMovement(patientName,adultWorkerRoom1.getDisplayName());
            else{
                instance.notifyMovement(patientName,adultWorkerRoom2.getDisplayName());
            }
            callCenter.notifyAvailable(ReleasedRoom.MDW_ADULT);
        }
        else if (room==adultWorkerRoom1 || room==adultWorkerRoom2){
            instance.notifyMovement(patient.getDisplayValue(),null); //goes to PYN, we only notify on entering CASHIER
            callCenter.notifyAvailable(ReleasedRoom.MDR_ADULT);
        }
        else if (room==childWorkerRoom1 || room==childWorkerRoom2){
            instance.notifyMovement(patient.getDisplayValue(),null);
            callCenter.notifyAvailable(ReleasedRoom.MDR_CHILD);
        }
        rl.unlock();

    }

    /**
     * Called by CCH to notify that some forward movement is expected
     * Will notify a patient in the correct room to start moving again
     * @param releasedRoom type of room that is now available
     */
    @Override
    public void notifyAvailable(ReleasedRoom releasedRoom) {
        if (! (releasedRoom.equals(ReleasedRoom.MDR_CHILD) || releasedRoom.equals(ReleasedRoom.MDR_ADULT))){
            throw new RuntimeException("Medical Hall was notified of the wrong movement: "+releasedRoom.name());
        }
        if (releasedRoom==ReleasedRoom.MDR_ADULT){
            rl.lock();
            if (inAdult){
                inAdult = false;
                adultWaitingRoom.setNext(getFreeAdultRoom());
                rl.unlock();
                adultWaitingRoom.notifyDone();
            }else{
                nextSlackAdult++;
                rl.unlock();
            }
        }
        if (releasedRoom==ReleasedRoom.MDR_CHILD){
            rl.lock();
            if (inChild){
                inChild = false;
                childWaitingRoom.setNext(getFreeChildRoom());
                rl.unlock();
                childWaitingRoom.notifyDone();
            }else{
                nextSlackChild++;
                rl.unlock();
            }
        }

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
     * As this hall itself is just a transition point there is no need to do anything here, see notifyDone() for call center signals
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient,IContainer next) {
        instance.notifyMovement(patient.getDisplayValue(), this.name); //patient has officially entered the actual waiting room
    }

    /**
     * Allow a waiting room patient to stop waiting without CCH call if we know there's space in MDR
     * @param room
     */
    @Override
    public void notifyWaiting(IWaitingRoom room) {
        rl.lock();
        if (room==childWaitingRoom){
            inChild = true;
            if (nextSlackChild>0){
                nextSlackChild--;
                inChild = false;
                room.setNext(getFreeChildRoom());
                room.notifyDone();
            }

        }
        else if (room==adultWaitingRoom){
            inAdult = true;
            if (nextSlackAdult>0){
                nextSlackAdult--;
                inAdult = false;
                room.setNext(getFreeAdultRoom());
                room.notifyDone();
            }
        }
        rl.unlock();
    }
}
