package cs.utd.soles.blob;

public class Blob {

    int x;

    String i;

    public Blob(int x, String i) {
        this.x = x;
        this.i = i;
    }

    public int divide() {
        return this.x / Integer.parseInt(i);
    }
}
