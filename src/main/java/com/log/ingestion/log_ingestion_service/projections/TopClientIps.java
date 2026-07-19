package com.log.ingestion.log_ingestion_service.projections;

public interface TopClientIps {
    String getClientIp();
    Long getIpCount();
}
