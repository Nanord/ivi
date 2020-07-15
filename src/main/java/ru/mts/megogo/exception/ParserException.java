package ru.mts.megogo.exception;

public class ParserException extends RuntimeException {

    public ParserException(String message) {
        super(message);
    }

    public ParserException() {
        super();
    }

    public ParserException(Throwable ex) {
        super(ex);
    }

    public ParserException(String message, Throwable ex) {
        super(message, ex);
    }
}