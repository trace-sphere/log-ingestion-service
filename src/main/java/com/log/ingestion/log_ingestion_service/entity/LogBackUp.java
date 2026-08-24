package com.log.ingestion.log_ingestion_service.entity;

import com.log.ingestion.log_ingestion_service.enums.Events;
import com.log.ingestion.log_ingestion_service.enums.LogLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name = "LOG_BACK_UP")
public class LogBackUp {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String backUpId;
    private String logId;
    private Instant timeStamp;
    private String traceId;
    private String serviceName;
    private String environment;
    private String host;
    private String instanceId;
    private Events eventType;
    private LogLevel level;
    private String thread;
    private String logger;
    private String message;
    private String spanId;
    private String method;
    private String path;
    private String status;
    private Integer durationMs;
    private String clientIp;
    private String userAgent;
    private String version;
    private String region;
    private String timeZone;
    private String exceptionId;
    private String exceptionClass;
    private String exceptionMessage;
    private String stackTrace;
    private String apiKey;
    private String operationStatus;
}
