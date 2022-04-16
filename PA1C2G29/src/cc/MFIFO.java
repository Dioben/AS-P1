package cc;

import java.lang.reflect.Array;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A FIFO queue copied from hc
 *
 * @param <T> the class of contained object
 */
public class MFIFO<T> {

    private final int size;
    private final T[] queue;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private int idxGet = 0;
    private int idxPut = 0;
    private int count = 0;
    private final Class<T> clazz; // for snapshot purposes

    public MFIFO(Class<T> clazz, int arraySize) {
        this.size = arraySize;
        this.clazz = clazz;
        queue = (T[]) Array.newInstance(clazz, size);
        rl = new ReentrantLock();
        cNotFull = rl.newCondition();
        cNotEmpty = rl.newCondition();
    }

    /**
     * Insert an object into the queue
     * <p>
     * This will block if the queue is currently full
     *
     * @param value inserted object
     */
    public void put(T value) {
        try {
            rl.lock();
            while (isFull())
                cNotFull.await();
            queue[idxPut] = value;
            idxPut = (idxPut + 1) % size;
            count++;
            cNotEmpty.signal();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            rl.unlock();
        }
    }

    /**
     * Remove an object from the queue
     * <p>
     * This will block if the queue is currently empty
     *
     * @return oldest object in this queue
     */
    public T get() {

        T val = null;
        try {
            rl.lock();
            while (isEmpty())
                cNotEmpty.await();
            val = queue[idxGet];
            idxGet = (idxGet + 1) % size;
            count--;
            cNotFull.signal();

        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            rl.unlock();
        }
        return val;
    }

    public boolean isFull() {
        return count == size;
    }

    public boolean isEmpty() {
        return count == 0;
    }
}
