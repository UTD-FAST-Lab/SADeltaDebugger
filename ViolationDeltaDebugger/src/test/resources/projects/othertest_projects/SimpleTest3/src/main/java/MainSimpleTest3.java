public class MainSimpleTest3 {

    public static void main(String[] args){

        System.out.println("Lets do some math!");

        Calculator c = new Calculator();
        BrokenCalculator bc = new BrokenCalculator();
        System.out.println(c.addition(1,1));
        System.out.println(bc.divide(1,1));
    }
}
