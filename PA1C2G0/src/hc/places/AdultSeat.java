package hc.places;

import hc.interfaces.IPatient;

public class AdultSeat extends Seat{

    IPatient user;

    @Override
    public boolean canSeat(IPatient patient) {
        return user==null && !patient.isChild();
    }

}
