package hc.places;

import hc.MFIFO;
import hc.active.TCommsHandler;
import hc.interfaces.IContainer;
import hc.interfaces.IPatient;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCenter extends Thread {
    //TODO: THIS DOES NOT SUPPORT PAUSING AT ALL RN
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
        onHold = new CallCenterRequest[people]; //only ever accessed by this thread, FIFO in theory but can't be sure

    }


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

    private void holdRequest(CallCenterRequest request) {
        onHold[held] = request;
        held++;
    }

    /**Releases ONE patient request based on patient ID
     * Adjusts the held request list so that there's never gaps
     *
     * @param ID: The allowed patient's ID
     */
    public void releaseRequest(String ID){
        CallCenterRequest request;
        for (int i=0;i<held;i++){
            request = onHold[i];

            if (request == null){
                break;
            }
            if (request.getID().equals(ID)){
                requests.put(request);
                onHold[i]=null;//empty spot, move array backwards
                i++;
                for (;i<held;i++){
                    request = onHold[i];
                    if (request==null)
                        break; //no more to fast forward
                    onHold[i-1] = onHold[i];
                }
                held--;

                return;
            }
        }
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

    public void run(){
        CallCenterRequest handling;
        while (true){
            handling = requests.get();
            handling.allowed = true;
            handling.condition.signalAll();

        }
    }


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
