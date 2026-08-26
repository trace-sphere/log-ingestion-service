package com.log.ingestion.log_ingestion_service.entity;

import com.log.ingestion.log_ingestion_service.enums.Events;
import com.log.ingestion.log_ingestion_service.enums.LogLevel;
import com.log.ingestion.log_ingestion_service.enums.OperationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name = "LOG_TRACE")
public class LogTrace {

    @EmbeddedId
    private LogPrimeKey logPrimaryKey;
    private String serviceName;
    private String environment;
    private String host;
    private String instanceId;

    @Enumerated(EnumType.STRING)
    private Events eventType;

    @Enumerated(EnumType.STRING)
    private LogLevel level;

    private String thread;
    private String logger;
    private String message;
    private String spanId;
    private LocalTime incomingTime;

    @Enumerated(EnumType.STRING)
    private OperationStatus elasticOperationStatus;

    @Enumerated(EnumType.STRING)
    private OperationStatus geoLocationOperationStatus;

    @JoinColumn(name = "exception_trace_id")
    @OneToOne(cascade = CascadeType.ALL)
    private ExceptionTrace exceptionTrace;

    @JoinColumn(name = "http_trace_id")
    @OneToOne(cascade = CascadeType.ALL)
    private HttpTrace httpTrace;

    @Embedded
    @Column(columnDefinition = "jsonb")
    private MetaDataTrace metaDataTrace;
}
