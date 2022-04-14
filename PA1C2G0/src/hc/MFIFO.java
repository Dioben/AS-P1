package hc;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MFIFO<T> implements hc.interfaces.IFIFO<T> {

    private final int size;
    private final T[] queue;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private int idxGet=0;
    private int idxPut=0;
    private int count = 0;
    private final Class<T> clazz; //for snapshot purposes

    public MFIFO(Class<T> clazz,int arraySize){
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
        } catch ( InterruptedException ignored ) {}
        finally {
            rl.unlock();
        }
    }

    @Override
    public T get() {

        T val = null;
        try{
            rl.lock();
            while ( isEmpty() )
                cNotEmpty.await();
            val = queue[idxGet];
            idxGet = (idxGet+1) % size;
            count--;
            cNotFull.signal();

        } catch( InterruptedException ignored ) {}
        finally {
            rl.unlock();
        }
        return val;
    }

    @Override
    public boolean isFull() {
        return count == size;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Generates a list snapshot of this array's current state, null-padded
     * @param size size of returned array
     * @return clone of this FIFO's content, oldest items first
     */
    @Override
    public T[] getSnapshot(int size){
        rl.lock();
        T[] values = (T[]) Array.newInstance(clazz, size);
        for (int i = 0;i<count;i++){
            if (i==size)
                break;
            values[i] = queue[(idxGet+i)%this.size];
        }

        rl.unlock();
        return values;
    }


}
