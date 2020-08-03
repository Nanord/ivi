package com.stm.ivi.exception;


import com.stm.ivi.retrying.RetryCount;

public class RetryException extends RuntimeException {

    public RetryException(String message, RetryCount retryCount, Throwable noCheckedException) {
        super(createMessage(message, retryCount), noCheckedException);
    }

    private static String createMessage(String message, RetryCount retryCount) {
        return String.format(
                "%sNumber of repetitions: %d/%d",
                message == null ? "" : String.format("%s, ", message),
                retryCount.getOriginNumberRetries() - retryCount.getCurrentNumberRetries(),
                retryCount.getOriginNumberRetries());
    }
}