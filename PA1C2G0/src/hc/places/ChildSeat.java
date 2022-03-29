package hc.places;

import hc.interfaces.IPatient;

public class ChildSeat extends Seat {

    IPatient user;

    @Override
    public boolean canSeat(IPatient patient) {
        return user==null && patient.isChild();
    }
}
