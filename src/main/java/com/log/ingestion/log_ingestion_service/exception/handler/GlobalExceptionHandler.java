package com.log.ingestion.log_ingestion_service.exception.handler;

import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import com.log.ingestion.log_ingestion_service.exception.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                     WebRequest request) {
        log.error("transactionId : {} => {}", request.getHeader("Transactionid"), ex.getMessage());

        List<JSONObject> errorList = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            JSONObject errorObject = new JSONObject();
            errorObject.put(error.getField(), error.getDefaultMessage());
            errorList.add(errorObject);
        }
        JSONObject result = new JSONObject();
        result.put("validation", errorList);
        ServiceResponse ServiceResponse = new ServiceResponse(true,
                messageSource.getMessage("request.validation.exception", null, LocaleContextHolder.getLocale()),
                List.of(result));
        return new ResponseEntity<>(ServiceResponse, HttpStatus.OK);
    }

    @ExceptionHandler(LogCreationException.class)
    public ResponseEntity<ServiceResponse> logCreationException(LogCreationException ex) {
        return new ResponseEntity<ServiceResponse>(
                ServiceResponse.builder()
                        .message(messageSource.getMessage(ex.getMessage(), null, Locale.ENGLISH))
                        .error(true)
                        .details(List.of())
                        .build(),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(ConditionalValidatorException.class)
    public ResponseEntity<ServiceResponse> validationException(ConditionalValidatorException ex) {
        Map<String, String> detailMap = Arrays.stream(ex.getMessage().split(","))
                .map(m -> Arrays.stream(m.split(":")).toList()).collect(Collectors.toMap(
                        item -> item.get(0),
                        item -> item.get(1),
                        (existing, duplicate) -> existing
                ));
        JSONObject detailsJson = new JSONObject(detailMap);
        return new ResponseEntity<ServiceResponse>(
                ServiceResponse.builder()
                        .message(
                                messageSource.getMessage("exception.common.validation", null, Locale.ENGLISH))
                        .error(true)
                        .details(List.of(detailsJson))
                        .build(),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ServiceResponse> logCreationException(ConstraintViolationException ex) {
        JSONObject response = new JSONObject();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            response.put(violation.getPropertyPath(), violation.getMessage());
        }
        return new ResponseEntity<ServiceResponse>(
                ServiceResponse.builder()
                        .message(
                                messageSource.getMessage("exception.common.validation", null, Locale.ENGLISH))
                        .error(true)
                        .details(List.of(response))
                        .build(),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(SearchSpecException.class)
    public ResponseEntity<ServiceResponse> logSearchException(SearchSpecException ex) {
        return new ResponseEntity<ServiceResponse>(
                ServiceResponse.builder()
                        .message(
                                messageSource.getMessage(ex.getMessage(), null, Locale.ENGLISH))
                        .error(true)
                        .details(List.of())
                        .build(),
                HttpStatus.OK
        );
    }


    @ExceptionHandler(ExternalApiRequestException.class)
    public ResponseEntity<ServiceResponse> handelExternalApiRequestException(ExternalApiRequestException ex) {
        return new ResponseEntity<ServiceResponse>(
                ServiceResponse.builder()
                        .message(
                                messageSource.getMessage(ex.getMessage(), null, Locale.ENGLISH))
                        .error(true)
                        .details(List.of())
                        .build(),
                HttpStatus.OK
        );
    }

    @ExceptionHandler(ElasticOperationException.class)
    public ResponseEntity<ServiceResponse> handelElasticOperationException(ElasticOperationException ex) {
        return new ResponseEntity<ServiceResponse>(
                ServiceResponse.builder()
                        .message(
                                messageSource.getMessage(ex.getMessage(), null, Locale.ENGLISH))
                        .error(true)
                        .details(List.of())
                        .build(),
                HttpStatus.OK
        );
    }
}
