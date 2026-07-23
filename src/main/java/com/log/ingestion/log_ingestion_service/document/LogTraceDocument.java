package com.log.ingestion.log_ingestion_service.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
@Document(indexName = "log_trace_document")
public class LogTraceDocument {

    @Field(type = FieldType.Keyword)
    @Id
    private String traceId;
    @Field(type = FieldType.Keyword)
    private String serviceName;
    @Field(type = FieldType.Keyword)
    private String environment;
    @Field(type = FieldType.Keyword)
    private String host;
    @Field(type = FieldType.Keyword)
    private String instanceId;
    @Field(type = FieldType.Keyword)
    private String eventType;
    @Field(type = FieldType.Keyword)
    private String level;
    @Field(type = FieldType.Text)
    private String thread;
    @Field(type = FieldType.Text)
    private String logger;
    @Field(type = FieldType.Text)
    private String message;
    @Field(type = FieldType.Keyword)
    private String spanId;
    @Field(type = FieldType.Date, format = DateFormat.hour_minute_second)
    private LocalTime incomingTime;
    @Field(type = FieldType.Text)
    private String exceptionClass;
    @Field(type = FieldType.Text)
    private String exceptionMessage;
    @Field(type = FieldType.Text)
    private String stackTrace;
    @Field(type = FieldType.Date)
    private LocalDateTime timeStamp;
    @Field(type = FieldType.Keyword)
    private String method;
    @Field(type = FieldType.Text)
    private String path;
    @Field(type = FieldType.Keyword)
    private String status;
    @Field(type = FieldType.Integer)
    private Integer durationMs;
    @Field(type = FieldType.Text)
    private String userAgent;
    @Field(type = FieldType.Version)
    private String version;
    @Field(type = FieldType.Ip)
    private String clientIp;
    @Field(type = FieldType.Keyword)
    private String region;
    @Field(type = FieldType.Keyword)
    private String country;
    @Field(type = FieldType.Keyword)
    private String city;
    @Field(type = FieldType.Double)
    private Double latitude;
    @Field(type = FieldType.Double)
    private Double longitude;
    @Field(type = FieldType.Keyword)
    private String zipCode;
    @Field(type = FieldType.Keyword)
    private String timeZone;
    @Field(type = FieldType.Keyword)
    private String countryCode;
    @Field(type = FieldType.Keyword)
    private String continentName;
    @Field(type = FieldType.Keyword)
    private String continentCode;
    @Field(type = FieldType.Long)
    private Long trafficCount;
}
