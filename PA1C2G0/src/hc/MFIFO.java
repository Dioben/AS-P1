package hc;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MFIFO<T> {

    private final int size;
    private final T[] queue;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private int idxGet=0;
    private int idxPut=0;
    private int count = 0;

    public MFIFO(Class<T> clazz,int arraySize){
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

    public T get() {

        T val = null;
        try{
            rl.lock();
            while ( isEmpty() )
                cNotEmpty.await();
            val = queue[idxGet];
            idxGet = (idxGet - 1) % size;
            count--;
            cNotFull.signal();

        } catch( InterruptedException ignored ) {}
        finally {
            rl.unlock();
        }
        return val;
    }

    private boolean isFull() {
        return count == size;
    }

    private boolean isEmpty() {
        return count == 0;
    }


}
