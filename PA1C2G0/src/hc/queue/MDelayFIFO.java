package hc.queue;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Similar to normal MFIFO<p>
 * Does not signal after get<p>
 * Instead a call to remove() with the given object must be used to release objects
 * @param <T> the class of the contained object
 */
public class MDelayFIFO<T> implements hc.interfaces.IDelayFIFO<T> {

    private final int size;
    private final T[] queue;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private int idxGet=0;
    private int idxPut=0;
    private int count = 0;
    private int awaitingRemoval = 0;
    private final Class<T> clazz; //for snapshot purposes

    public MDelayFIFO(Class<T> clazz, int arraySize){
        this.size = arraySize;
        this.clazz = clazz;
        queue = (T[]) Array.newInstance(clazz, size);
        rl = new ReentrantLock();
        cNotFull = rl.newCondition();
        cNotEmpty = rl.newCondition();
    }

    @Override
    public void put(T value) {
        try {
            rl.lock();
            while ( isFull() )
                cNotFull.await();
            queue[ idxPut ] = value;
            idxPut = (idxPut+1)%size;
            count++;
            cNotEmpty.signal();
        } catch ( InterruptedException ignored ) {
            Thread.currentThread().interrupt();
        }
        finally {
            rl.unlock();
        }
    }

    /**
     * Difference from normal implementation: does not signal available space
     * @return object at idxGet
     */
    @Override
    public T get() {

        T val = null;
        try{
            rl.lock();
            while ( isEmpty() )
                cNotEmpty.await();
            val = queue[idxGet];
            idxGet = (idxGet+1) % size;
            awaitingRemoval++;
            count--;

        } catch( InterruptedException ignored ) {
            Thread.currentThread().interrupt();
        }
        finally {
            rl.unlock();
        }
        return val;
    }

    /**
     * Signals that one of the objects that has been gotten before has now actually left
     * It does not matter which one left
     */
    @Override
    public void remove(){
        try{
            rl.lock();
            awaitingRemoval--;
            cNotFull.signal();

            } finally {
            rl.unlock();
        }
    }

    /**
     * Generates a list snapshot of this array's current state, null-padded
     * Includes items awaiting remove
     * @param size size of returned array
     * @return clone of this FIFO's content, oldest items first
     */
    @Override
    public T[] getSnapshot(int size){
        rl.lock();
        T[] values = (T[]) Array.newInstance(clazz, size);
        int idx;
        for (int i = 0;i<count+awaitingRemoval;i++){
            if (i==size)
                break;
            idx = (idxGet-awaitingRemoval+i)%this.size;
            if (idx<0)
                idx = this.size+idx;
            values[i] = queue[idx];
        }

        rl.unlock();
        return values;
    }




    @Override
    public boolean isFull() {
        return count+awaitingRemoval == size;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }


}
