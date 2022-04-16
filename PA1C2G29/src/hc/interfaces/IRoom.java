package hc.interfaces;

public interface IRoom extends IContainer {
    /**
     * Used by Medical Hall due to ambiguous following room policy
     * 
     * @param next container marker as following this Room
     */
    void setNext(IContainer next);
}
