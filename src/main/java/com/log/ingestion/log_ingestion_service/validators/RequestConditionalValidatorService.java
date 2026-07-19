package com.log.ingestion.log_ingestion_service.validators;


import com.log.ingestion.log_ingestion_service.dto.ExceptionTraceValidator;
import com.log.ingestion.log_ingestion_service.dto.HttpTraceValidator;
import jakarta.validation.Valid;


public interface RequestConditionalValidatorService {
    void validateEventType(@Valid ExceptionTraceValidator exceptionTraceValidator);
    void validateHttpEventType(@Valid HttpTraceValidator httpTraceValidator);
}
