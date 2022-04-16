package hc.interfaces;

/**
 * Interface for a FIFO queue
 * @param <T> the class of contained object
 */
public interface IFIFO<T> {
    /**
     * Insert an object into the queue<p>
     * This will block if the queue is currently full
     * @param value inserted object
     */
    void put(T value);

    /**
     * Remove an object from the queue <p>
     * This will block if the queue is currently empty
     * @return oldest object in this queue
     */
    T get();

    boolean isFull();

    boolean isEmpty();
    /**
     * Get oldest n objects
     * @param size size of obtained list
     * @return list of objects, may have NULL padding
     */
    T[] getSnapshot(int size);
}
