package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class LogTraceDocumentDto {
    private String traceId;
    private String serviceName;
    private String environment;
    private String host;
    private String instanceId;
    private String eventType;
    private String level;
    private String thread;
    private String logger;
    private String message;
    private String spanId;
    private LocalTime incomingTime;
    private String exceptionClass;
    private String exceptionMessage;
    private String stackTrace;
    private LocalDateTime timeStamp;
    private String method;
    private String path;
    private String status;
    private Integer durationMs;
    private String userAgent;
    private String version;
    private String clientIp;
    private String region;
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;
    private String zipCode;
    private String timeZone;
    private String countryCode;
    private String continentName;
    private String continentCode;
    private Long trafficCount;
}
