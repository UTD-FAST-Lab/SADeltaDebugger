public class cgManyTest {

    public static void main(String[] args) {
        A();
    }

    public static void A() {
        B();
        F();
    }

    public static void B() {
        
        C();
        F();
    }

    public static void C() {
        D();
        F();
    }

    public static void D() {
        E();
        F();
    }

    public static void E() {
        F();
    }

    public static void F() {
        // failure
        System.out.println(10 / 0);
    }
}
