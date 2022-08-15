package cs.utd.soles.vehicle;

import cs.utd.soles.vehicle.Vehicle;

public class Car extends Vehicle {

    public Car(int gas, int mpg) {
        super(gas, mpg);
    }
    @Override
    public int drive(int miles) {
        return gas/mpg;
    }
}
