package com.log.ingestion.log_ingestion_service.projections;

public interface AvgResponseByService {
    Long getAvgResponseTime();
    String getServiceName();
    Long getNoOfHit();
}
