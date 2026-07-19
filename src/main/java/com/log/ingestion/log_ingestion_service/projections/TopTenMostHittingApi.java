package com.log.ingestion.log_ingestion_service.projections;

public interface TopTenMostHittingApi {
    Long getNoOfHits();
    String getPath();
    Long getAvgResponseTime();
    String getMethod();
}
