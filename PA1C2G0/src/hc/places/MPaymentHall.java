package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.Severity;
import hc.enums.Worker;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MPaymentHall implements IHall {

    private final  HCInstance instance;
    private final IWorkerRoom cashierRoom;
    private final String name = "PYH";
    private final ReentrantLock rl;
    private final Condition cashierAvailableSignal;
    private int entered = 0; //ID tracker
    private int released = -1;
    private boolean cashierAvailable = true;
    private final MFIFO<IPatient> backlog;
    private final String nextRoomName = "OUT";

    /**Instances a Payment Hall
     *
     * @param instance Space this hall is contained in
     * @param after Follow-up room, NULL is expected
     * @param people amount of people expected to pass through this room
     */
    public MPaymentHall(HCInstance instance,IContainer after, int people){
        this.instance = instance;
        cashierRoom = WorkerRoom.getRoom(Worker.CASHIER,this,null,"PYR");
        rl = new ReentrantLock();
        backlog = new MFIFO(IPatient.class,people);
        cashierAvailableSignal = rl.newCondition();

    }

    /**
     * Called by patient after they've managed to get to hallway's entrance<p>
     * Will return the cashier room once patient's turn is up
     * @param patient patient attempting to find next room
     * @return
     */
    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        rl.lock();
        if (!cashierAvailable || !backlog.isEmpty()){
            backlog.put(patient);
            while (released != patient.getRoomNumber()){
                try {
                    cashierAvailableSignal.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        cashierAvailable = false;
        rl.unlock();
        return  cashierRoom;
    }


    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * Returns this container's occupation status, including contained patients for all sub-containers
     * @return Map(room names, patientID[])
     */
    @Override
    public Map<String, String[]> getState() {
        Map<String,String[]> states = new HashMap<>();
        IPatient[] patients = backlog.getSnapshot(entered - released);
        String[] state = new String[patients.length];
        for(int i=0;i< patients.length;i++){
            IPatient patient = patients[i];
            if (patient==null)
                    break;
            state[i] = patient.getDisplayValue();
        }
        states.put(this.name,state);
        states.putAll(cashierRoom.getState());

        return states;
    }

    /**
     * Pause all contained threads<p>
     * Due to patient pooling this only affects the contained cashier
     */
    @Override
    public void suspend() {
        cashierRoom.suspend();
    }

    /**
     * Resume all contained threads<p>
     * Due to patient pooling this only affects the contained cashier
     */
    @Override
    public void resume() {
        cashierRoom.resume();
    }

    /**
     * Propagates interrupt to cashier
     */
    @Override
    public void interrupt() {
    cashierRoom.interrupt();
    }

    /**
     * Called by contained room to notify that patient is out
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room,IPatient left) {
        rl.lock();

        instance.notifyMovement(left.getDisplayValue(),nextRoomName); //notify patient removal
        if (!backlog.isEmpty()) {
            IPatient patient = backlog.get();
            released = patient.getRoomNumber();
            cashierAvailable = false;
            cashierAvailableSignal.signalAll();
        }else{
            cashierAvailable = true;
        }

        rl.unlock();

    }


    @Override
    public HCInstance getInstance() {
        return instance;
    }

    /**
     * Allow patient to enter this Hall<p>
     * Automatically sets their room number and increments counter
     * @param patient the patient attempting to enter the space
     */
    @Override
    public void enter(IPatient patient) {
        rl.lock();
        patient.setPaymentNumber(entered);
        entered++;
        rl.unlock();
    }

    /**
     * Notifies that a patient has left this hall and entered the waiting rooms<p>
     * Used for logging purposes
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient, IContainer next) {
        instance.notifyMovement(patient.getDisplayValue(),this.name); //has entered cashier
    }

}
