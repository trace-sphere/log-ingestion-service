package com.log.ingestion.log_ingestion_service.exception;

public class ElasticOperationException extends RuntimeException{
    public ElasticOperationException() {
    }

    public ElasticOperationException(String message) {
        super(message);
    }

    public ElasticOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticOperationException(Throwable cause) {
        super(cause);
    }

    public ElasticOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
