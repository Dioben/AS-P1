package hc;

import hc.active.TCommsHandler;
import hc.interfaces.IHall;
import hc.places.MCallCenter;
import hc.places.MEntranceHall;
import hc.places.MEvaluationHall;

public class HCInstance {

    private final Timer timer;
    private final MCallCenter callCenter;
    private final IHall entranceHall;
    private final IHall evaluationHall;
    private final IHall waitingHall;
    private final IHall medicalHall;
    private final IHall paymentHall;
    private boolean paused = false;

    public HCInstance(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime, TCommsHandler tCommsHandler, boolean mode) {
        timer = new Timer.Builder()
                .withEvaluationTimeRange(evalTime)
                .withAppointmentTimeRange(medicTime)
                .withPaymentTimeRange(payTime)
                .withMovementTimeRange(getUpTime).build();
        callCenter = new MCallCenter(false, tCommsHandler,adults+children);
        callCenter.start();

        waitingHall = null;
        medicalHall = null;
        paymentHall = null;

        evaluationHall = new MEvaluationHall(this,waitingHall,callCenter);
        MEntranceHall eh = new MEntranceHall(this,evaluationHall,seats, adults,children,4);
        entranceHall = eh;
        callCenter.setEntranceHall(eh);
        //callCenter.setMedicalHall(medicalHall);
        //callCenter.setWaitingHall(waitingHall);




    }

    public void permitMovement(String patientID) {
        callCenter.releaseRequest(patientID);

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
        //TODO: MANUALLY KILL EVERY THREAD
    }

    public boolean isPaused() {//TODO: IMPLEMENT PAUSE FUNCTIONALITY
        return paused;
    }

    public Timer getTimer() {
        return  timer;
    }
}
