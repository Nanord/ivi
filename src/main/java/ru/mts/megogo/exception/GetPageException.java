package ru.mts.megogo.exception;

public class GetPageException extends RuntimeException {

    public GetPageException(String message) {
        super(message);
    }

    public GetPageException() {
        super();
    }

    public GetPageException(Throwable ex) {
        super(ex);
    }

    public GetPageException(String message, Throwable ex) {
        super(message, ex);
    }
}
