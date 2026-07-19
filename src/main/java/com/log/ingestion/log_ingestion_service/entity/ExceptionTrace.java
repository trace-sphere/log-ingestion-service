package com.log.ingestion.log_ingestion_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@Entity
@Table(name = "EXCEPTION_TRACE")
public class ExceptionTrace {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String exceptionId;
    private String traceId;
    private String exceptionClass;
    private String exceptionMessage;
    private String stackTrace;
    private Timestamp timeStamp;
}
