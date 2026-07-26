package com.log.ingestion.log_ingestion_service.exception;

public class DataFetchException extends RuntimeException{
    public DataFetchException() {
    }

    public DataFetchException(String message) {
        super(message);
    }

    public DataFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataFetchException(Throwable cause) {
        super(cause);
    }

    public DataFetchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
