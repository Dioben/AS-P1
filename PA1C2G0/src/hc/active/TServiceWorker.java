package hc.active;

import hc.utils.Timer;
import hc.interfaces.IPatient;
import hc.interfaces.IWorkerRoom;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Generic class for service workers (Nurse/Cashier/Doctor)
 * Implements every method except ServeCustomer()
 */
public abstract class TServiceWorker extends Thread implements hc.interfaces.IServiceWorker {
    protected Timer timer;// protected for use in subclasses
    private final IWorkerRoom surroundings;
    private IPatient customer;
    private final ReentrantLock rl;
    private final Condition c;

    /**
     * Waits for a customer to be assigned to itself
     * <p>
     * Serves customer
     * <p>
     * Removes customer from self
     * <p>
     * Warns container that they're done
     */
    private void handleNextCostumer() {
        try {
            rl.lock();
            while (customer == null && !Thread.interrupted()) {
                try {
                    c.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            serveCustomer(customer);
            customer = null;
            surroundings.notifyDone();// allow patient to getNextRoom
        } finally {
            rl.unlock();
        }
    }

    public TServiceWorker(Timer timer, IWorkerRoom surroundings) {
        this.timer = timer;
        this.surroundings = surroundings;
        rl = new ReentrantLock();
        c = rl.newCondition();
    }

    /**
     * Infinitely waits for next patient to show up
     */
    public void run() {
        while (!Thread.interrupted()) {
            handleNextCostumer();
        }
    }

    /**
     * Attempts to provide a patient to this worker
     * 
     * @param patient the provided patient
     * @return whether the patient was accepted, not expected to fail
     */
    public boolean providePatient(IPatient patient) {
        boolean val = false;
        try {
            rl.lock();
            if (!isBusy()) {
                this.customer = patient;
                c.signal();
                val = true;
            }
        } finally {
            rl.unlock();
        }
        return val;
    }

    public boolean isBusy() {
        return customer != null;
    }

    abstract void serveCustomer(IPatient patient);
}
