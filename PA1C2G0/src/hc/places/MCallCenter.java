package hc.places;

import hc.MFIFO;
import hc.active.TCommsHandler;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * STINKY CLASS
 * REQUIRES A FULL REWRITE
 */
public class MCallCenter extends Thread {
    //TODO: THIS SUCKS AND DOES NOT CONSIDER TELLING CC THAT YOU'RE AVAILABLE AT ALL
    //TODO: REPLACE THE ONE FIFO WITH ONE FIFO PER CONDITION PROBABLY
    //TODO: ADD A CONDITION THAT AWAKENS THIS MAIN THREAD MAYBE
    //TODO ALTERNATIVE: MOVE FROM 1 THREAD TOTAL TO 4 THREADS-> every thread has an entrance queue with built in "slack" and matches one of our current conditions

    private final TCommsHandler comms;
    private final ReentrantLock rl;
    private final Condition evrAvailable;
    private final Condition wtrAvailable;
    private final Condition mdwAvailable;
    private final Condition mdrAvailable;
    private final MFIFO<CallCenterRequest> requests;
    private boolean manual;
    private  CallCenterRequest[] onHold;
    private int held = 0;

    public MCallCenter(boolean manual, TCommsHandler tCommsHandler,int people){
        rl = new ReentrantLock();
        evrAvailable = rl.newCondition();
        wtrAvailable = rl.newCondition();
        mdwAvailable = rl.newCondition();
        mdrAvailable = rl.newCondition();
        this.manual = manual;
        comms = tCommsHandler;
        requests = new MFIFO(CallCenterRequest[].class,people);
        onHold = new CallCenterRequest[people]; //not necessarily FIFO, lock-protected

    }

    /**
     * Sets this class's operation mode
     * @param b True for manual operation, False for automatic mode
     */
    public void setManual(boolean b) {
    manual = b;
    }

    public CallCenterRequest registerRequest(IContainer container, IPatient patient){
        Condition cond = getMatchingCondition(container);
        if (cond==null){
            throw new RuntimeException("This should not be possible, name was"+ container.getDisplayName());
        }
        CallCenterRequest request = new CallCenterRequest(patient,cond);
        if (! manual)
            requests.put(request);
        else {
            holdRequest(request);
            comms.requestPermission(patient.getDisplayValue(), container.getDisplayName());
        }
        return request;
    }

    /**
     * Sets a request to be temporarily held until controller approves it
     * @param request: the request to be held
     */
    private void holdRequest(CallCenterRequest request) {
        rl.lock();
        onHold[held] = request;
        held++;
        rl.unlock();
    }

    /**Called by communication socket after movement is approved
     * Releases ONE patient request based on patient ID
     * Adjusts the held request list so that there's never gaps
     * If patient isn't on the backlog nothing will happen
     *
     * @param ID: The allowed patient's ID
     */
    public void releaseRequest(String ID){
        rl.lock();
        CallCenterRequest request;
        for (int i=0;i<held;i++){
            request = onHold[i];

            if (request == null){
                break; //nothing found
            }
            if (request.getID().equals(ID)){ //found
                requests.put(request); //actually move it into the queue
                onHold[i]=null;//empty spot, move array backwards
                i++;
                for (;i<held;i++){
                    if (onHold[i]==null)
                        break; //no more to fast forward
                    onHold[i-1] = onHold[i];
                }
                held--;
                rl.unlock();
                return;
            }
        }
        rl.unlock();//nothing found
    }

    private Condition getMatchingCondition(IContainer container) {
        String name = container.getDisplayName();
        if (name.startsWith("ETR"))
            return evrAvailable;
        if (name.equals("WTH"))
            return wtrAvailable;
        if (name.startsWith("WTR"))
            return mdwAvailable;
        if (name.startsWith("MDW"))
            return mdrAvailable;
        return null;
    }

    /**
     * gets the latest request, allow it
     * big problem: this does not match spec at all
     */
    public void run(){
        CallCenterRequest handling;
        while (true){
            handling = requests.get();
            handling.allowed = true;
            handling.condition.signalAll();

        }
    }

    /**
     * Class for holding information that is fed back to a customer:
     * Contains a condition they must wait on and a boolean to confirm that they're meant to wake up
     */
    public class CallCenterRequest {
        private final IPatient patient;
        private boolean allowed = false;
        private final Condition condition;


        private CallCenterRequest(IPatient p, Condition cond){
            patient = p;
            condition = cond;
        }

        public boolean isAllowed() {
            return allowed;
        }
        public Condition getCondition(){
        return  condition;
        }
        public String getID(){
            return patient.getDisplayValue();
        }
    }

    }
