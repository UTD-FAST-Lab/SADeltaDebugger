public class cgRecursionTest {

    public static void main(String[] args) {
        recur(10);
        // failure
        System.out.println(10 / 0);

    }

    public static int recur(int i) {
        if (i <= 0) {
            return 0;
        }
        return recur(i - 1);
    }
}
