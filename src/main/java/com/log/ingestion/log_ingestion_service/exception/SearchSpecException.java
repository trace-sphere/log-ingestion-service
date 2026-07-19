package com.log.ingestion.log_ingestion_service.exception;

public class SearchSpecException extends RuntimeException{
    public SearchSpecException() {
    }

    public SearchSpecException(String message) {
        super(message);
    }

    public SearchSpecException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchSpecException(Throwable cause) {
        super(cause);
    }

    public SearchSpecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
