package hc.places;

import hc.HCInstance;
import hc.active.*;
import hc.enums.Worker;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Room meant to hold Doctor/Nurse/Cashier and a single patient at a time
 * The worker is a thread but the room itself is not
 */
public class WorkerRoom implements IWorkerRoom,ISeat {
    private final IHall container;
    private IContainer next;
    private IPatient user;
    private IServiceWorker worker;
    private final String name;


    protected WorkerRoom(IHall container, IContainer next, String name){
        this.container = container;
        this.next = next;
        this.name = name;
    }

    /**
     * Used to inject a worker inside factory method
     * @param worker The worker that will reside in room
     */
    private void setWorker(IServiceWorker worker){
        this.worker = worker;
    }


    public boolean canEnter() {
        return user==null;
    }



    /**
     * notifies container that this room is empty
     * if parent room has anyone waiting they tell that one thread to enter and keep every other one waiting
     * @param patient individual leaving space
     *
     */
    @Override
    public void leave(IPatient patient)
    {
        if (patient==user){
            user = null;
            container.notifyDone(this);
        }
    }

    /**
     * notifies current patient that it should start trying to leave
     * at this stage user is expected to be waiting at getFollowingContainer
     */
    @Override
    public void notifyDone() {
        user.notify();
    }

    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        if (! user.equals(patient))
            throw new RuntimeException("Patient getting worker follower does not match contained");
        worker.providePatient(user);
        while(worker.isBusy()){
            try {
                user.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return next;
    }
    /**
     * This method is locked by the parent container
     * @param patient the patient attempting to enter the space
     */
    @Override
    public void enter(IPatient patient) {
        user = patient;
    }


    @Override
    public String getDisplayName() {
        return name;
    }


    /**
     *
     * @return string detailing room's contained patient
     */
    @Override
    public Map<String, String[]> getState() {
        String[] info = {user==null?"": user.getDisplayValue()};
        HashMap<String, String[]> val = new HashMap<>();
        val.put(this.name,info);
        return val;
    }

    /**
     * pause all activity
     */
    @Override
    public void suspend() {
        worker.suspend();
    }

    /**
     * Resume paused activity
     */
    @Override
    public void resume() {
        worker.resume();
    }

    public static WorkerRoom getRoom(Worker worker, IHall container, IContainer next, String name){
        if (worker==null)
            return null;
        WorkerRoom workerRoom = new WorkerRoom(container,next,name);
        TServiceWorker workerThread = null;
        HCInstance instance = container.getInstance();
        if (worker==Worker.DOCTOR){
            workerThread = new TDoctor(instance.getTimer(),workerRoom);
        }
        else if (worker==Worker.NURSE){
            workerThread = new TNurse(instance.getTimer(),workerRoom);
        }
        else if (worker==Worker.CASHIER){
            workerThread = new TCashier(instance.getTimer(),workerRoom);
        }
        workerRoom.setWorker(workerThread);
        workerThread.start();
        return workerRoom;
    }
    @Override
    public IPatient getUser(){return user;}

    @Override
    public void setNext(IContainer next) {
        this.next = next;
    }
}
