package hc.interfaces;

public interface IFIFO<T> {
    void put(T value);

    T get();

    boolean isFull();

    boolean isEmpty();

    T[] getSnapshot(int size);
}
