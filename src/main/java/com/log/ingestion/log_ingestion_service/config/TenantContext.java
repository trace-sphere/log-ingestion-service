package com.log.ingestion.log_ingestion_service.config;

public class TenantContext {
    public static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
}
