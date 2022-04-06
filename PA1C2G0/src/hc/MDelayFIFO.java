package hc;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Similar to normal MFIFO
 * does not signal after get
 * Instead a call to remove() with the given object must be used to release objects
 * @param <T>
 */
public class MDelayFIFO<T> {

    private final int size;
    private final T[] queue;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private int idxGet=0;
    private int idxPut=0;
    private int count = 0;
    private int awaitingRemoval = 0;

    public MDelayFIFO(Class<T> clazz, int arraySize){
        this.size = arraySize;
        queue = (T[]) Array.newInstance(clazz, size);
        rl = new ReentrantLock();
        cNotFull = rl.newCondition();
        cNotEmpty = rl.newCondition();
    }

    public void put( T value ) {
        try {
            rl.lock();
            while ( isFull() )
                cNotFull.await();
            queue[ idxPut ] = value;
            idxPut = (idxPut+1)%size;
            count++;
            cNotEmpty.signal();
        } catch ( InterruptedException ignored ) {}
        finally {
            rl.unlock();
        }
    }

    /**
     * Difference from normal implementation: does not signal available space
     * @return object at idxGet
     */
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

        } catch( InterruptedException ignored ) {}
        finally {
            rl.unlock();
        }
        return val;
    }

    /**
     * Signals that one of the objects that has been gotten before has now actually left
     * it does not matter which one left
     */
    public void remove(){
        try{
            rl.lock();
            awaitingRemoval--;
            cNotFull.signal();

            } finally {
            rl.unlock();
        }
    }



    public boolean isFull() {
        return count+awaitingRemoval == size;
    }

    public boolean isEmpty() {
        return count == 0;
    }


}
