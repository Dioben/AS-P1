package hc.interfaces;

/**
 * Interface for a FIFO queue
 * @param <T> The class of contained object
 */
public interface IFIFO<T> {
    /**
     * Insert an object into the queue<p>
     * This will block if the queue is currently full
     * @param value Inserted object
     */
    void put(T value);

    /**
     * Remove an object from the queue <p>
     * This will block if the queue is currently empty
     * @return Oldest object in this queue
     */
    T get();

    boolean isFull();

    boolean isEmpty();
    /**
     * Get oldest n objects
     * @param size Size of obtained list
     * @return List of objects, may have NULL padding
     */
    T[] getSnapshot(int size);
}
