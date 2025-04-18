package utils;

public class MalformedMoveException extends Exception {

    public MalformedMoveException(String message) {
        super(message);
    }

    public MalformedMoveException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = 1L;

}
