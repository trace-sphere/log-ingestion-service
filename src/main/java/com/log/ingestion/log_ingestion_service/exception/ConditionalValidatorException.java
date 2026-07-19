package com.log.ingestion.log_ingestion_service.exception;

public class ConditionalValidatorException extends RuntimeException{
    public ConditionalValidatorException() {
    }

    public ConditionalValidatorException(String message) {
        super(message);
    }

    public ConditionalValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditionalValidatorException(Throwable cause) {
        super(cause);
    }

    public ConditionalValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
