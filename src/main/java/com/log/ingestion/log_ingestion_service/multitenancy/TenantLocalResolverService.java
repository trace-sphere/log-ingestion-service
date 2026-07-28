package com.log.ingestion.log_ingestion_service.multitenancy;

public interface TenantLocalResolverService {
    public String getCurrentTenant();
}
