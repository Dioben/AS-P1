package hc;

public class HCInstance {

    private final Timer timer;
    public HCInstance(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime) {
        timer = new Timer.Builder()
                .withEvaluationTimeRange(evalTime)
                .withAppointmentTimeRange(medicTime)
                .withPaymentTimeRange(payTime)
                .withMovementTimeRange(getUpTime).build();
    }

    public void permitMovement(int patientID, String destination) {
    }

    public void setControls(String s) {
    }

    public void progress() {
    }

    public void pause() {
    }

    public void cleanUp() {
    }

    public boolean isPaused() {//TODO: IMPLEMENT PAUSE FUNCTIONALITY
        return false;
    }

    public Timer getTimer() {
        return  timer;
    }
}
