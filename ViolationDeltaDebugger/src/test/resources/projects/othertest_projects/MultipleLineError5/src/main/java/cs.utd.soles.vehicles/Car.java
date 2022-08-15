public class Car extends Vehicle{

    public Car(int gas, int mpg) {
        super(gas, mpg);
    }
    @Override
    public int drive(int miles) {
        return gas/mpg;
    }
}
