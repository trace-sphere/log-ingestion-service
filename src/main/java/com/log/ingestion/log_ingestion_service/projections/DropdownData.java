package com.log.ingestion.log_ingestion_service.projections;

public interface DropdownData {
    String getServiceName();
    String getLevel();
    String getMethod();
    String getEventType();
    String getStatus();
}
