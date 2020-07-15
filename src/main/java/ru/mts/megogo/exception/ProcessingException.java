package ru.mts.megogo.exception;

public class ProcessingException extends Exception {

    public ProcessingException(String message, Throwable ex) {
        super(message, ex);
    }

}
