package hc.interfaces;

public interface IHall extends IContainer{
    /**
     * called by contained rooms to inform that they have finished their processing if any
     * @param room identifies room that has finished processing
     */
    void notifyDone(IRoom room);
}
