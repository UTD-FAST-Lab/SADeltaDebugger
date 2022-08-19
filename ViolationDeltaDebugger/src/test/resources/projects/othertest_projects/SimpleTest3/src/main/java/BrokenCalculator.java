public class BrokenCalculator extends AbstractCalculator{


    @Override
    public int divide(int x, int y) {
        return x/0;
    }

    @Override
    public int addition(int x, int y) {
        return x+y;
    }
}
