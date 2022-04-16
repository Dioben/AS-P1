package hc.interfaces;

/**
 * Interface for a FIFO queue where objects have a 'removal delay'<p>
 * Every get() must be followed by a remove() to truly clear an object from the queue
 * @param <T> the class of contained object
 */
public interface IDelayFIFO<T> {
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

    /**
     * Signal that a pending removal has finished
     */
    void remove();

    /**
     * Get oldest n objects, including ones in removal pending status
     * @param size size of obtained list
     * @return list of objects, may have NULL padding
     */
    T[] getSnapshot(int size);

    boolean isFull();

    boolean isEmpty();
}
