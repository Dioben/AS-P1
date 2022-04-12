package hc;

import hc.active.TCommsHandler;
import hc.active.TPatient;
import hc.interfaces.IContainer;
import hc.interfaces.IHall;
import hc.interfaces.IPatient;
import hc.places.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class representing an instance of health center
 * Includes all halls and patients
 */
public class HCInstance {

    private final Timer timer;
    private final MCallCenter callCenter;
    private final IHall entranceHall;
    private final IHall evaluationHall;
    private final IHall waitingHall;
    private final IHall medicalHall;
    private final IHall paymentHall;
    private final ReentrantLock loggerAccess;
    private final HCPLogger logger;
    private final int adults;
    private final int children;
    private boolean started= false;
    private int gone = 0;
    private final int seats;
    private final IPatient[] patients;

    public HCInstance(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime, TCommsHandler tCommsHandler, boolean mode) {
        this.adults = adults;
        this.children = children;
        this.seats = seats;

        patients = new IPatient[adults+children];

        logger = new HCPLogger();
        loggerAccess = new ReentrantLock();


        timer = new Timer.Builder()
                .withEvaluationTimeRange(evalTime)
                .withAppointmentTimeRange(medicTime)
                .withPaymentTimeRange(payTime)
                .withMovementTimeRange(getUpTime).build();


        callCenter = new MCallCenter(mode, tCommsHandler,adults+children);
        callCenter.start();

        paymentHall = new MPaymentHall(this,adults+children);
        MMedicalHall mh = new MMedicalHall(this,paymentHall,callCenter);
        medicalHall = mh;
        MWaitingHall wh = new MWaitingHall(this, medicalHall, seats, adults, children, 1, 1, callCenter);
        waitingHall = wh;
        evaluationHall = new MEvaluationHall(this,waitingHall,callCenter);
        MEntranceHall eh = new MEntranceHall(this,evaluationHall,seats, adults,children,4);
        entranceHall = eh;

        callCenter.setEntranceHall(eh);
        callCenter.setMedicalHall(mh);
        callCenter.setWaitingHall(wh);

    }

    /**
     * Instances and starts all patients
     * Attempts to randomize whether a patient is a child while possible
     */
    public void start(){
        if (started)
            throw new RuntimeException("HC Instance was already started");
        started = true;
        logger.printHeader(adults,children,seats);
        Random r = new Random();
        TPatient patient;
        int assignedChildren = 0;
        int assignedAdults = 0;
        boolean isChild;
        for (int i=0;i<adults+children;i++){
            if (assignedAdults==adults)
                isChild=true;
            else if (assignedChildren==children)
                isChild=false;
            else{
                isChild = r.nextBoolean();
            }
            assignedChildren+= isChild?1:0;
            assignedAdults+= isChild?0:1;
            patient = new TPatient(isChild,timer,entranceHall);
            patients[i]=patient;
            patient.start();
        }


        if (assignedAdults>adults)
            throw new RuntimeException("Somehow too many adults were instanced");
        if (assignedChildren>children)
            throw new RuntimeException("Somehow too many children were instanced");
    }


    public void permitMovement(String roomID) {
        callCenter.releaseRequest(roomID);

    }

    public void setControls(String s) {
        if (s.equals("MANUAL")){
            callCenter.setManual(true);
        }else if (s.equals("AUTO")){
            callCenter.setManual(false);
        }
        logger.printState(s);
    }

    public void progress() {
        paymentHall.resume();
        medicalHall.resume();
        waitingHall.resume();
        evaluationHall.resume();
        entranceHall.resume();
        for(IPatient p : patients){
            if (p.isAlive())
                p.resume();
        }
        logger.printState("RESUME");
    }

    public void pause() {
        paymentHall.suspend();
        medicalHall.suspend();
        waitingHall.suspend();
        evaluationHall.suspend();
        entranceHall.suspend();

        for(IPatient p : patients){
            if (p.isAlive())
                p.suspend();
        }
        logger.printState("PAUSE");
    }

    public void cleanUp() {
        //TODO: MANUALLY KILL EVERY THREAD or just STOP EXISTING, who cares really
    }

    public Timer getTimer() {
        return  timer;
    }

    /**
     * Gets all room statuses and passes them to logger/UI
     *
     */
    public void notifyMovement(String patient, String room){
        loggerAccess.lock();
        if (room!=null) //movements that don't warrant logging can happen
            {
                if (room.equals("OUT"))
                    gone++;
                logger.printPosition(room,patient);
            }
        updateUI();
        loggerAccess.unlock();

        if (gone==adults+children) {
            callCenter.notifyOver();
        }

    }

    private void updateUI() {
        Map<String,String[]> UIInfo = new HashMap<>();
        UIInfo.putAll(entranceHall.getState());
        UIInfo.putAll(evaluationHall.getState());
        UIInfo.putAll(waitingHall.getState());
        UIInfo.putAll(medicalHall.getState());
        UIInfo.putAll(paymentHall.getState());

        /* TODO: INJECT A UI CLASS INTO THIS AND GIVE IT AN UPDATE METHOD
        if (display!=null)
            display.update(UIInfo);

         */
    }
}
