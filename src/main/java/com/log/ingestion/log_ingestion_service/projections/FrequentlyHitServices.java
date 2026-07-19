package com.log.ingestion.log_ingestion_service.projections;

public interface FrequentlyHitServices {
    Long getHittingTime();
    String getServiceName();
}
