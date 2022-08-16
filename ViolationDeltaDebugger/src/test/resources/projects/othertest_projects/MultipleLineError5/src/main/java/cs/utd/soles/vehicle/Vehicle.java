package cs.utd.soles.vehicle;

public class Vehicle {
    public int gas;
    public int mpg;

    public Vehicle(int gas, int mpg){
        this.gas=gas;
        this.mpg=mpg;
    }

    public int drive(int miles){
        return gas - mpg / miles;
    }

    public int range(){
        return gas*mpg;
    }
}
