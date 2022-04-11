package hc.places;

import hc.HCInstance;
import hc.MFIFO;
import hc.enums.ReleasedRoom;
import hc.enums.Worker;
import hc.interfaces.*;

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
    private boolean cashierAvailable = false;
    private final MFIFO<IPatient> backlog;

    public MPaymentHall(HCInstance instance,int people){
        this.instance = instance;
        cashierRoom = WorkerRoom.getRoom(Worker.CASHIER,this,null,"PYH");
        rl = new ReentrantLock();
        backlog = new MFIFO(IPatient[].class,people);
        cashierAvailableSignal = rl.newCondition();

    }

    /**
     * Called by patient after they've managed to get to hallway's entrance
     * Will return the cashier room once patient's turn is up
     * @param patient patient attempting to find next room
     * @return
     */
    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        rl.lock();
        if (!cashierAvailable){
            backlog.put(patient);
            while (released< patient.getRoomNumber()){
                try {
                    cashierAvailableSignal.await();
                } catch (InterruptedException e) {}
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
        cashierAvailable = true;
        //TODO: logging here
        IPatient patient = backlog.get();
        released = patient.getRoomNumber();
        cashierAvailableSignal.signal();
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
        patient.setPaymentNumber(entered);
        patient.setRoomNumber(entered);
        entered++;
        rl.unlock();
    }

    /**
     * Notifies that a patient has left this hall and entered the waiting rooms
     * Used for logging purposes
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient) {
        //TODO: log patient migration
    }

}
