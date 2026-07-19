package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

import java.sql.Timestamp;


@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
public class SearchRequestDto {
    private String status;
    private String eventType;
    private String method;
    private Integer durationMs;
    private String serviceName;
    private String level;
    private String firstTimeStamp;
    private String lastTimeStamp;
}
