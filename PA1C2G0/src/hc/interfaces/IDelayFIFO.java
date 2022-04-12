package hc.interfaces;

public interface IDelayFIFO<T> {
    void put(T value);

    T get();

    void remove();

    T[] getSnapshot(int size);

    boolean isFull();

    boolean isEmpty();
}
