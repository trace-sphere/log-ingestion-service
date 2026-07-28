package com.log.ingestion.log_ingestion_service.multitenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }

    @Override
    public void initialize() {

    }
}
