package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DashboardAnalyticsDto {

    private long totalLogs;
    private double successRate;
    private double averageResponseTime;

    private Map<String, Long> eventCounts;
    private Map<String, Long> statusCounts;
    private Map<String, Long> levelCounts;

}