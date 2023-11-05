package com.itskool.exceptions;

public class EventProcessingException extends RuntimeException {

    public EventProcessingException() {
    }

    public EventProcessingException(String errorMessage) {
        super(errorMessage);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventProcessingException(Throwable cause) {
        super(cause);
    }
}
