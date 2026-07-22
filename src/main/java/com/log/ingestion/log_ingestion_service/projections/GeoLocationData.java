package com.log.ingestion.log_ingestion_service.projections;

public interface GeoLocationData {
    String getCity();
    String getRegion();
    String getLatitude();
    String getLongitude();
    String getAppTrafficCount();
}
