package hc.interfaces;

public interface ILogger {
    void printHeader(int adults, int children, int seats);

    void printPosition(String place, String patient);

    void printState(String state);
}
