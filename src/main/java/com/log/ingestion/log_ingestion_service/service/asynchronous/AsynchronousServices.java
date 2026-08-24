package com.log.ingestion.log_ingestion_service.service.asynchronous;

import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;
import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;

public interface AsynchronousServices {
    public void saveUserGeoLocation(LogPrimeKey logPrimeKey, String clientIp, String apiKey);

    public void saveDataToElasticSearch(LogTrace logTrace, String apiKey);

    public void takeBackUpWithRollBack(LogRequestDto logRequestDto);
}
