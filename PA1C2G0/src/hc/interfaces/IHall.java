package hc.interfaces;

import hc.HCInstance;

public interface IHall extends IContainer{
    /**
     * called by contained rooms to inform that they have finished their processing if any
     * @param room identifies room that has finished processing
     */
    void notifyDone(IRoom room,IPatient patient);

    /**
     * Get the instance this container is a part of
     * @return instance container belongs to
     */
    public HCInstance getInstance();
}
