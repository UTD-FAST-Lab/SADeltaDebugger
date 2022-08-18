public class MainSimpleTest2 {


    public static int divideBy(int x, int y){

        return x/y;
    }

    public static void hello(){
        System.out.println("Hello, World! Let's Do Math!");
    }

    public static void main(String[] args){

        hello();

        System.out.println("100 / 10 is what?");

        System.out.println(divideBy(100,0));
    }

}
