package hc;

import hc.active.TCommsHandler;
import hc.active.TPatient;
import hc.interfaces.IHall;
import hc.interfaces.IPatient;
import hc.places.*;

import java.util.Random;

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
    private final int adults;
    private final int children;
    private boolean paused = false;
    private boolean started= false;

    public HCInstance(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime, TCommsHandler tCommsHandler, boolean mode) {
        this.adults = adults;
        this.children = children;

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
        Random r = new Random();
        IPatient patient;
        int assignedChildren = 0;
        int assignedAdults = 0;
        boolean isChild = false;
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
            patient.run();

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
    }

    public void progress() {
        paused = false;
        //TODO: PROPAGATE CONTINUE SOMEHOW
    }

    public void pause() {
        paused = true;
        //TODO: PROPAGATE PAUSE SOMEHOW
    }

    public void cleanUp() {
        //TODO: MANUALLY KILL EVERY THREAD or just STOP EXISTING, who cares really
    }

    public boolean isPaused() {//TODO: IMPLEMENT PAUSE FUNCTIONALITY
        return paused;
    }

    public Timer getTimer() {
        return  timer;
    }
}
