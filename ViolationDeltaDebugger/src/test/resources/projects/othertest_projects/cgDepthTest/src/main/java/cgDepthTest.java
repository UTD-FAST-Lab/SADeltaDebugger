public class cgDepthTest {

    public static void main(String[] args) {
        int variable = assignment();
        if((variable == 0)){
            A();
        }
    }

    public static int assignment(){
        return 0;
    }

    public static void A() {
        B();
    }

    public static void B() {
        // failure
        System.out.println(10 / 0);
        C();
    }

    public static void C() {
        D();
    }

    public static void D() {
        E();
    }

    public static void E() {
        F();
    }

    public static void F() {
        System.out.println("reached F");
        return;
    }
}
