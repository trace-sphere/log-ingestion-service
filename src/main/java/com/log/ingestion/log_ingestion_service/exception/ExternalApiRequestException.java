package com.log.ingestion.log_ingestion_service.exception;

public class ExternalApiRequestException extends RuntimeException{
    public ExternalApiRequestException() {
    }

    public ExternalApiRequestException(String message) {
        super(message);
    }

    public ExternalApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalApiRequestException(Throwable cause) {
        super(cause);
    }

    public ExternalApiRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
