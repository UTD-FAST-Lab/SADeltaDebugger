package cs.utd.soles.util;

public class SanityException extends Exception{
    public SanityException() {
    }

    public SanityException(String s) {
        super(s);
    }

    public SanityException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SanityException(Throwable throwable) {
        super(throwable);
    }
}
