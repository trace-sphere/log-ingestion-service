package com.log.ingestion.log_ingestion_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
public class ExceptionTraceValidator {

    @NotNull(message = "{exception.class.not.null}")
    @NotBlank(message = "{exception.class.not.blank}")
    private String exceptionClass;
    @NotNull(message = "{exception.msg.not.null}")
    @NotBlank(message = "{exception.msg.not.blank}")
    private String exceptionMessage;
    @NotNull(message = "{exception.trace.not.null}")
    @NotBlank(message = "{exception.trace.not.blank}")
    private String stackTrace;

    @NotNull(message = "{timestamp.not.null}")
    private Instant timeStamp;
}
