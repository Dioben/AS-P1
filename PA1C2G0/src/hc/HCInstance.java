package hc;

import hc.active.TCommsHandler;
import hc.places.MCallCenter;
import hc.places.MHall;

public class HCInstance {

    private final Timer timer;
    private final MCallCenter callCenter;
    private final MHall entranceHall;
    private final MHall evaluationHall;
    private final MHall waitingHall;
    private final MHall medicalHall;
    private final MHall paymentHall;
    private boolean paused = false;
    public HCInstance(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime, TCommsHandler tCommsHandler) {
        timer = new Timer.Builder()
                .withEvaluationTimeRange(evalTime)
                .withAppointmentTimeRange(medicTime)
                .withPaymentTimeRange(payTime)
                .withMovementTimeRange(getUpTime).build();
        callCenter = new MCallCenter(false, tCommsHandler,adults+children);
        callCenter.start();

        entranceHall = null;
        evaluationHall = null;
        waitingHall = null;
        medicalHall = null;
        paymentHall = null;


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
