package com.log.ingestion.log_ingestion_service.projections;

public interface PeakTrafficHours {
    Long getRequestCount();
    Long getHours();
}
