package com.log.ingestion.log_ingestion_service.exception;

public class LogAuthenticationException extends RuntimeException {
    public LogAuthenticationException() {
    }

    public LogAuthenticationException(String message) {
        super(message);
    }

    public LogAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogAuthenticationException(Throwable cause) {
        super(cause);
    }

    public LogAuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
