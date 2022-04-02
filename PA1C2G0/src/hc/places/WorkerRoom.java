package hc.places;

import hc.HCInstance;
import hc.active.*;
import hc.enums.Worker;
import hc.interfaces.*;

public class WorkerRoom implements IRoom { //rooms for nurse,doctor,cashier: only supports one person at a time
    //subclasses implement age lock policy

    private final IHall container;
    private final IContainer next;
    private IPatient user;
    private IServiceWorker worker;
    private final String name;

    protected WorkerRoom(IHall container, IContainer next, String name){
        this.container = container;
        this.next = next;
        this.name = name;
    }
    private void setWorker(IServiceWorker worker){
        this.worker = worker;
    }
    @Override
    public boolean canEnter(TPatient patient) {
        return user==null;
    }

    @Override
    public boolean enter(TPatient patient) {//not supposed to ever actually return false
        if (canEnter(patient)){
            user = patient;
            return  true;
        }
        return false;
    }

    @Override
    public void leave() {
        user = null;
    }

    @Override
    public void notifyDone() {
        //TODO: FIGURE THIS OUT, PROBABLY HAS SOMETHING TO DO WITH CALL CENTER
    }

    @Override
    public IContainer getFollowingContainer() {
        worker.providePatient(user);
        return next;
    }

    @Override
    public void tryEnter(TPatient tPatient) {
    //TODO: FIGURE THIS OUT, PROBABLY HAS SOMETHING TO DO WITH CALL CENTER
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public HCInstance getInstance() {
        return container.getInstance();
    }

    @Override
    public String getState() {
        if (user == null)
            return "";
        return user.getDisplayValue();
    }

    public static WorkerRoom getRoom(Worker worker, IHall container, IContainer next, String name){
        if (worker==null)
            return null;
        WorkerRoom workerRoom = null;
        TServiceWorker workerThread = null;
        HCInstance instance = container.getInstance();
        if (worker==Worker.DOCTOR){
            workerRoom = new AdultWorkerRoom(container,next,name);
            workerThread = new TDoctor(instance.getTimer(),instance,workerRoom);
        }
        else if (worker==Worker.CHILD_DOCTOR){
            workerRoom = new ChildWorkerRoom(container,next,name);
            workerThread = new TDoctor(instance.getTimer(),instance,workerRoom);
        }
        else if (worker==Worker.NURSE){
            workerRoom = new WorkerRoom(container,next,name);
            workerThread = new TNurse(instance.getTimer(),instance,workerRoom);
        }
        else if (worker==Worker.CASHIER){
            workerRoom = new WorkerRoom(container,next,name);
            workerThread = new TCashier(instance.getTimer(),instance,workerRoom);
        }
        workerRoom.setWorker(workerThread);
        workerThread.start();
        return workerRoom;
    }
    public IPatient getUser(){return user;}
}
