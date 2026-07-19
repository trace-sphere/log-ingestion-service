package com.log.ingestion.log_ingestion_service.exception;

public class LogCreationException extends RuntimeException {
    public LogCreationException() {
    }

    public LogCreationException(String message) {
        super(message);
    }

    public LogCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogCreationException(Throwable cause) {
        super(cause);
    }

    public LogCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
