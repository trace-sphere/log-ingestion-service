package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;

public interface AsynchronousServices {
    public void saveUserGeoLocation(LogPrimeKey logPrimeKey, String clientIp);
}
