package hc.places;

import hc.interfaces.IPatient;
import hc.interfaces.ISeat;

public class Seat implements ISeat {

    IPatient user;

    @Override
    public boolean canSeat(IPatient patient) {
        return user==null;
    }

    @Override
    public boolean seat(IPatient patient) {
        if (canSeat(patient)){
            user = patient;
            return  true;
        }
        return  false;
    }

    @Override
    public void leave() {
        user = null;
    }
}
