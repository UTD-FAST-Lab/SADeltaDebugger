public class Truck extends Vehicle{
    
    public Truck(int gas, int mpg) {
        super(gas, mpg);
    }
    
    @Override
    public int drive(int miles) {
        return 0;
    }
}
