package hc.places;

import hc.HCInstance;
import hc.queue.MFIFO;
import hc.enums.ReleasedRoom;
import hc.enums.Severity;
import hc.interfaces.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MWaitingHall implements IWaitingHall, ICallCenterWaiter {

    private final HCInstance instance;
    private final ICallCenterWaiter callCenter; // must be set later due to object creation flow
    private final IWaitingRoom childRoom;
    private final IWaitingRoom adultRoom;
    private final String name = "WTH";
    private int inChild = 0;
    private int inAdult = 0;
    private int assignedAdult = 0; // separate variables due to travel time
    private int assignedChild = 0;
    private final int roomMax;
    private final ReentrantLock rl;
    private final Condition childRoomAvailable;
    private final Condition adultRoomAvailable;
    private int entered = 0; // ID tracker

    private int releasedBlueAdult = -1; // helps patients know if they can leave
    private int releasedYellowAdult = -1; // helps patients know if they can leave
    private int releasedRedAdult = -1; // helps patients know if they can leave
    private int releasedBlueChild = -1; // helps patients know if they can leave
    private int releasedYellowChild = -1; // helps patients know if they can leave
    private int releasedRedChild = -1; // helps patients know if they can leave

    private final IFIFO<IPatient> childBacklogRed;
    private final IFIFO<IPatient> adultBacklogRed;
    private final IFIFO<IPatient> childBacklogYellow;
    private final IFIFO<IPatient> adultBacklogYellow;
    private final IFIFO<IPatient> childBacklogBlue;
    private final IFIFO<IPatient> adultBacklogBlue;
    private int nextSlackAdult; // we start out with 1 adult slots available in MDW
    private int nextSlackChild; // we start out with 1 child slots available in MDW
    private final int adults;
    private final int children;

    /**
     * Instances a Waiting Hall
     * 
     * @param instance           space this hall is contained in
     * @param after              follow-up container, <i>NULL</i> is expected
     * @param seatsPerRoom       number of seats in each contained room
     * @param adults             number of expected adults
     * @param children           number of expected children
     * @param nextRoomSlackAdult number of adult slots in next room
     * @param nextRoomSlackChild number of child slots in next room
     * @param callCenter         entity that must be notified when someone leaves
     *                           contained subspaces
     */
    public MWaitingHall(HCInstance instance, IContainer after, int seatsPerRoom, int adults, int children,
            int nextRoomSlackAdult, int nextRoomSlackChild, ICallCenterWaiter callCenter) {
        this.instance = instance;
        childRoom = new PriorityWaitingRoom(this, after, "WTR2", seatsPerRoom);
        adultRoom = new PriorityWaitingRoom(this, after, "WTR1", seatsPerRoom);
        roomMax = seatsPerRoom;
        rl = new ReentrantLock();
        childRoomAvailable = rl.newCondition();
        adultRoomAvailable = rl.newCondition();
        childBacklogRed = new MFIFO(IPatient.class, children);
        adultBacklogRed = new MFIFO(IPatient.class, adults);
        childBacklogYellow = new MFIFO(IPatient.class, children);
        adultBacklogYellow = new MFIFO(IPatient.class, adults);
        childBacklogBlue = new MFIFO(IPatient.class, children);
        adultBacklogBlue = new MFIFO(IPatient.class, adults);
        nextSlackAdult = nextRoomSlackAdult;
        nextSlackChild = nextRoomSlackChild;
        this.callCenter = callCenter;
        this.adults = adults;
        this.children = children;
    }

    /**
     * Called by patient after they've managed to get to hallway's entrance
     * <p>
     * Will direct patient to correct room but then bar them from entering at all
     * 
     * @param patient patient attempting to find next room
     * @return waiting room matching user with free space
     */
    @Override
    public IContainer getFollowingContainer(IPatient patient) {
        if (patient.isChild())
            return enterChildRoom(patient);
        return enterAdultRoom(patient);
    }

    /**
     * Called by patient
     * <p>
     * Move into adult room as soon as it is available
     * 
     * @return this hall's adult room
     * @param patient patient moving in, current Thread
     */
    private IContainer enterAdultRoom(IPatient patient) {
        rl.lock();
        IFIFO backlog = getBacklog(patient);
        if (assignedAdult == roomMax || !backlog.isEmpty()) {
            backlog.put(patient);
            instance.notifyMovement(patient.getDisplayValue(), null);
            while (getControlNumber(patient) < patient.getRoomNumber()) {
                try {
                    adultRoomAvailable.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        assignedAdult++;
        rl.unlock();
        return adultRoom;

    }

    /**
     * Called by patient
     * <p>
     * Move into child room as soon as it is available
     * 
     * @return this hall's child room
     * @param patient patient moving in, current Thread
     */
    private IContainer enterChildRoom(IPatient patient) {
        rl.lock();
        IFIFO backlog = getBacklog(patient);
        if (assignedChild == roomMax || !backlog.isEmpty()) {
            backlog.put(patient);
            instance.notifyMovement(patient.getDisplayValue(), null);
            while (getControlNumber(patient) < patient.getRoomNumber()) {
                try {
                    childRoomAvailable.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        assignedChild++;
        rl.unlock();
        return childRoom;
    }

    /**
     * Returns the appropriate release value based on patient severity
     * 
     * @param patient patient that the return value must be relevant to
     *                child/severity wise
     * @return WTN of the youngest released user in this severity group
     */
    private int getControlNumber(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (patient.isChild()) {
            if (severity.equals(Severity.BLUE))
                return releasedBlueChild;
            if (severity.equals(Severity.RED))
                return releasedRedChild;
            if (severity.equals(Severity.YELLOW))
                return releasedYellowChild;
        }
        if (severity.equals(Severity.BLUE))
            return releasedBlueAdult;
        if (severity.equals(Severity.RED))
            return releasedRedAdult;
        if (severity.equals(Severity.YELLOW))
            return releasedYellowAdult;
        throw new RuntimeException("Unassigned patient in WTH");
    }

    /**
     * Returns the user's queue based on severity and age
     * 
     * @param patient user
     * @return a backlog for user to wait in
     */
    private IFIFO getBacklog(IPatient patient) {
        Severity severity = patient.getSeverity();
        if (severity.equals(Severity.UNASSIGNED))
            throw new RuntimeException("Unassigned patient in WTH");

        if (patient.isChild()) {
            if (severity.equals(Severity.BLUE))
                return childBacklogBlue;
            if (severity.equals(Severity.RED))
                return childBacklogRed;
            if (severity.equals(Severity.YELLOW))
                return childBacklogYellow;
        } else {
            if (severity.equals(Severity.BLUE))
                return adultBacklogBlue;
            if (severity.equals(Severity.RED))
                return adultBacklogRed;
            if (severity.equals(Severity.YELLOW))
                return adultBacklogYellow;

        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * Reports this container's state for UI purposes, prioritizing older items
     * <p>
     * Key WTH will return 6 items, 3 children first then 3 adults
     * 
     * @return Map(Room Name, patientID[])
     */
    @Override
    public Map<String, String[]> getState() {
        Map<String, String[]> states = new HashMap<>();
        IPatient[] adultsRed = adultBacklogRed.getSnapshot(adults);
        IPatient[] adultsYellow = adultBacklogYellow.getSnapshot(adults);
        IPatient[] adultsBlue = adultBacklogBlue.getSnapshot(adults);
        IPatient[] childrenRed = childBacklogRed.getSnapshot(children);
        IPatient[] childrenYellow = childBacklogYellow.getSnapshot(children);
        IPatient[] childrenBlue = childBacklogBlue.getSnapshot(children);
        IPatient[][] patients = new IPatient[][] {
                adultsRed, adultsYellow, adultsBlue, childrenRed, childrenYellow, childrenBlue
        };
        String[] adultsRedState = new String[adultsRed.length];
        String[] adultsYellowState = new String[adultsYellow.length];
        String[] adultsBlueState = new String[adultsBlue.length];
        String[] childrenRedState = new String[childrenRed.length];
        String[] childrenYellowState = new String[childrenYellow.length];
        String[] childrenBlueState = new String[childrenBlue.length];
        String[][] patientsState = new String[][] {
                adultsRedState, adultsYellowState, adultsBlueState, childrenRedState, childrenYellowState,
                childrenBlueState
        };

        for (int i = 0; i < patients.length; i++) {
            for (int j = 0; j < patients[i].length; j++) {
                IPatient patient = patients[i][j];
                if (patient == null)
                    break;
                patientsState[i][j] = patient.getDisplayValue();
            }
        }

        states.put(this.name + "AR", adultsRedState);
        states.put(this.name + "AY", adultsYellowState);
        states.put(this.name + "AB", adultsBlueState);
        states.put(this.name + "CR", childrenRedState);
        states.put(this.name + "CY", childrenYellowState);
        states.put(this.name + "CB", childrenBlueState);
        states.putAll(childRoom.getState());
        states.putAll(adultRoom.getState());

        return states;
    }

    /**
     * Pause all contained threads
     * <p>
     * Due to patient pooling this method does not actually do anything
     */
    @Override
    public void suspend() {

    }

    /**
     * Resume all contained threads
     * <p>
     * Due to patient pooling this method does not actually do anything
     */
    @Override
    public void resume() {

    }

    /**
     * Due to patient thread pooling there is no need to interrupt patients here
     */
    @Override
    public void interrupt() {

    }

    /**
     * Called by contained room to notify that patient is out of WTR, calls CCH so
     * that it can notify others
     * 
     * @param room identifies room that has finished processing
     */
    @Override
    public void notifyDone(IRoom room, IPatient patient) {
        if (room == childRoom) {
            callCenter.notifyAvailable(ReleasedRoom.WTR_CHILD);
        } else if (room == adultRoom) {
            callCenter.notifyAvailable(ReleasedRoom.WTR_ADULT);
        }
    }

    /**
     * Identifies highest-priority child patient in queue if they exist and tells
     * them to leave, updates containment state
     * <p>
     * Locked from parent method
     */
    private void handleChildRoomLeave() {
        assignedChild--;
        if (!childBacklogRed.isEmpty()) {
            IPatient patient = childBacklogRed.get();
            int rn = patient.getRoomNumber();
            releasedRedChild = releasedRedChild > rn ? releasedRedChild : rn;
        } else if (!childBacklogYellow.isEmpty()) {
            IPatient patient = childBacklogYellow.get();
            int rn = patient.getRoomNumber();
            releasedYellowChild = releasedYellowChild > rn ? releasedYellowChild : rn;
        } else if (!childBacklogBlue.isEmpty()) {
            IPatient patient = childBacklogBlue.get();
            int rn = patient.getRoomNumber();
            releasedBlueChild = releasedBlueChild > rn ? releasedBlueChild : rn;
        }

        childRoomAvailable.signalAll();
    }

    /**
     * Identifies highest-priority adult patient in queue if they exist and tells
     * them to leave, updates containment state
     * <p>
     * Locked from parent method
     */
    private void handleAdultRoomLeave() {
        assignedAdult--;
        if (!adultBacklogRed.isEmpty()) {
            IPatient patient = adultBacklogRed.get();
            int rn = patient.getRoomNumber();
            releasedRedAdult = releasedRedAdult > rn ? releasedRedAdult : rn;
        } else if (!adultBacklogYellow.isEmpty()) {
            IPatient patient = adultBacklogYellow.get();
            int rn = patient.getRoomNumber();
            releasedYellowAdult = releasedYellowAdult > rn ? releasedYellowAdult : rn;
        } else if (!adultBacklogBlue.isEmpty()) {
            IPatient patient = adultBacklogBlue.get();
            int rn = patient.getRoomNumber();
            releasedBlueAdult = releasedBlueAdult > rn ? releasedBlueAdult : rn;
        }
        adultRoomAvailable.signalAll();

    }

    /**
     * Called by CCH to notify that some forward movement is expected
     * 
     * @param releasedRoom type of room, only WTR or MDW variants are valid
     */
    @Override
    public void notifyAvailable(ReleasedRoom releasedRoom) {
        if (!(releasedRoom.equals(ReleasedRoom.WTR_ADULT) || releasedRoom.equals(releasedRoom.WTR_CHILD) ||
                releasedRoom.equals(releasedRoom.MDW_ADULT) || releasedRoom.equals(releasedRoom.MDW_CHILD))) {
            throw new RuntimeException("Waiting Hall was notified of the wrong movement: " + releasedRoom.name());
        }
        if (releasedRoom.equals(ReleasedRoom.MDW_ADULT)) {
            rl.lock();
            if (inAdult == 0) {
                nextSlackAdult++;
                rl.unlock();
            } else {
                inAdult--;
                rl.unlock();
                adultRoom.notifyDone();
            }
        } else if (releasedRoom.equals(ReleasedRoom.MDW_CHILD)) {
            rl.lock();
            if (inChild == 0) {
                nextSlackChild++;
                rl.unlock();
            } else {
                inChild--;
                rl.unlock();
                childRoom.notifyDone();
            }
        } else if (releasedRoom.equals(ReleasedRoom.WTR_ADULT)) {
            rl.lock();
            if (assignedAdult == 0) {
                rl.unlock();
                throw new RuntimeException("Adult somehow left empty WTR");
            }
            handleAdultRoomLeave();
            rl.unlock();
        } else if (releasedRoom.equals(ReleasedRoom.WTR_CHILD)) {
            rl.lock();
            if (assignedChild == 0) {
                rl.unlock();
                throw new RuntimeException("Child somehow left empty WTR");
            }
            handleChildRoomLeave();
            rl.unlock();

        }

    }

    @Override
    public HCInstance getInstance() {
        return instance;
    }

    /**
     * Allow patient to enter this Hall
     * <p>
     * Automatically sets their room number and increments counter
     * 
     * @param patient the patient attempting to enter the space
     */
    @Override
    public void enter(IPatient patient) {
        rl.lock();
        patient.setWaitingNumber(entered);
        entered++;
        rl.unlock();
    }

    /**
     * Notifies that a patient has left this hall and entered the waiting rooms
     * <p>
     * As the counters were preemptively increased earlier and this class does not
     * signal Call Center this function does nothing
     * 
     * @param patient individual leaving space
     */
    @Override
    public void leave(IPatient patient, IContainer room) {
        instance.notifyMovement(patient.getDisplayValue(), room.getDisplayName());
    }

    /**
     * Allow a waiting room patient to stop waiting without CCH call if we know
     * there's space in MDW
     * 
     * @param room room that a patient is now waiting inside of
     */
    @Override
    public void notifyWaiting(IWaitingRoom room) {
        if (room == childRoom) {
            rl.lock();
            if (nextSlackChild > 0) {
                nextSlackChild--;
                rl.unlock();
                room.notifyDone();
                return;
            }
            inChild++;
            rl.unlock();
        } else if (room == adultRoom) {
            rl.lock();
            if (nextSlackAdult > 0) {
                nextSlackAdult--;
                rl.unlock();
                room.notifyDone();
                return;
            }
            inAdult++;
            rl.unlock();
        }
    }
}
