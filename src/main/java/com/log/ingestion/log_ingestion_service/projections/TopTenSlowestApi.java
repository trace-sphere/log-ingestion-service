package com.log.ingestion.log_ingestion_service.projections;

public interface TopTenSlowestApi {
    String getPath();

    Integer getDurationMs();

    String getHttpId();

    String getTraceId();

    String getMethod();
}
