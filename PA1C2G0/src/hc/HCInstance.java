package hc;

import hc.active.TCommsHandler;
import hc.active.TPatient;
import hc.interfaces.IHall;
import hc.interfaces.ILogger;
import hc.interfaces.IPatient;
import hc.places.*;
import hc.utils.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing an instance of health center
 * Includes all halls and patients
 * Must be started manually
 */
public class HCInstance {

    private final Timer timer;
    private final MTCallCenter callCenter;
    private final IHall entranceHall;
    private final IHall evaluationHall;
    private final IHall waitingHall;
    private final IHall medicalHall;
    private final IHall paymentHall;
    private final ILogger logger;
    private final int adults;
    private final int children;
    private boolean started = false;
    private AtomicInteger gone = new AtomicInteger(0);
    private final int seats;
    private final IPatient[] patients;
    private final TGUI display;

    /**
     * Instance a health center
     * 
     * @param adults        number of adults
     * @param children      number of children
     * @param seats         number of seats
     * @param evalTime      max nurse evaluation time
     * @param medicTime     max doctor appointment time
     * @param payTime       max payment time
     * @param getUpTime     max movement time
     * @param tCommsHandler container that handles contact with controller
     * @param mode          whether this program is starting in manual or automatic
     *                      mode
     * @param gui           the UI object
     * @param logger        a logger implementation
     */
    public HCInstance(int adults, int children, int seats, int evalTime, int medicTime, int payTime, int getUpTime,
                      TCommsHandler tCommsHandler, boolean mode, TGUI gui, ILogger logger) {
        this.adults = adults;
        this.children = children;
        this.seats = seats;

        patients = new IPatient[adults + children];

        this.logger = logger;

        timer = new Timer.Builder()
                .withEvaluationTimeRange(evalTime)
                .withAppointmentTimeRange(medicTime)
                .withPaymentTimeRange(payTime)
                .withMovementTimeRange(getUpTime).build();

        callCenter = new MTCallCenter(mode, tCommsHandler, adults + children);
        callCenter.start();

        paymentHall = new MPaymentHall(this, null, adults + children);
        MMedicalHall mh = new MMedicalHall(this, paymentHall, callCenter);
        medicalHall = mh;
        MWaitingHall wh = new MWaitingHall(this, medicalHall, seats, adults, children, 1, 1, callCenter);
        waitingHall = wh;
        evaluationHall = new MEvaluationHall(this, waitingHall, callCenter);
        MEntranceHall eh = new MEntranceHall(this, evaluationHall, seats, adults, children, 4);
        entranceHall = eh;

        callCenter.setEntranceHall(eh);
        callCenter.setMedicalHall(mh);
        callCenter.setWaitingHall(wh);

        display = gui;
        logger.printHeader(adults, children, seats);
        logger.printState("INITIALIZE");
    }

    /**
     * Instances and starts all patients
     * <p>
     * Attempts to randomize whether a patient is a child while possible
     */
    public void start() {
        if (started)
            throw new RuntimeException("HC Instance was already started");
        started = true;
        Random r = new Random();
        int assignedChildren = 0;
        int assignedAdults = 0;
        boolean isChild;
        for (int i = 0; i < adults + children; i++) {
            if (assignedAdults == adults)
                isChild = true;
            else if (assignedChildren == children)
                isChild = false;
            else {
                isChild = r.nextBoolean();
            }
            assignedChildren += isChild ? 1 : 0;
            assignedAdults += isChild ? 0 : 1;
            patients[i] = new TPatient(isChild, timer, entranceHall);
            ((TPatient) patients[i]).start();
        }

        if (assignedAdults > adults)
            throw new RuntimeException("Somehow too many adults were instanced");
        if (assignedChildren > children)
            throw new RuntimeException("Somehow too many children were instanced");
    }

    /**
     * Propagate a movement allowance notification from rooms
     * 
     * @param roomID name of the room
     */
    public void permitNotification(String roomID) {
        callCenter.releaseRequest(roomID);
    }

    public void setControls(String s) {
        if (s.equals("MANUAL")) {
            callCenter.setManual(true);
        } else if (s.equals("AUTO")) {
            callCenter.setManual(false);
        }
    }

    /**
     * Unpause this instance
     */
    public void progress() {
        callCenter.resume();
        paymentHall.resume();
        medicalHall.resume();
        waitingHall.resume();
        evaluationHall.resume();
        entranceHall.resume();
        for (IPatient p : patients) {
            if (p.isAlive())
                p.resume();
        }
    }

    /**
     * Pause this instance
     */
    public void pause() {
        callCenter.suspend();
        paymentHall.suspend();
        medicalHall.suspend();
        waitingHall.suspend();
        evaluationHall.suspend();
        entranceHall.suspend();

        for (IPatient p : patients) {
            if (p.isAlive())
                p.suspend();
        }
    }

    /**
     * Make all contained threads shut down elegantly
     */
    public void cleanUp() {
        callCenter.interrupt();
        entranceHall.interrupt();
        evaluationHall.interrupt();
        waitingHall.interrupt();
        medicalHall.interrupt();
        paymentHall.interrupt();
        for (IPatient p : patients) {
            if (p.isAlive())
                p.interrupt();
        }
        display.setLoadingScreen();
    }

    public Timer getTimer() {
        return timer;
    }

    /**
     * Gets all room statuses and passes them to logger/UI
     */
    public void notifyMovement(String patient, String room) {
        if (room != null) // movements that don't warrant logging can happen
        {
            if (room.equals("OUT"))
                gone.getAndIncrement();
            logger.printPosition(room, patient);
        }
        updateUI();

        if (gone.get() == adults + children) {
            callCenter.notifyOver();
        }

    }

    /**
     * Obtains all relevant system information and passes it to the render thread
     */
    private void updateUI() {
        Map<String, String[]> UIInfo = new HashMap<>();
        UIInfo.putAll(entranceHall.getState());
        UIInfo.putAll(evaluationHall.getState());
        UIInfo.putAll(waitingHall.getState());
        UIInfo.putAll(medicalHall.getState());
        UIInfo.putAll(paymentHall.getState());

        if (display != null)
            display.update(UIInfo);
    }
}
