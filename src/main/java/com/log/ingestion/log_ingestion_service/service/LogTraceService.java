package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.dto.*;

import java.time.Instant;

public interface LogTraceService {
    public ServiceResponse saveLog(LogRequestDto logRequestDto);

    public SearchResponse searchBySpec(SearchRequestDto requestBody, int pageNo, int pageSize);

    public SearchResponse getAllCardData();

    public ServiceResponse getGraphData(GraphDataFilterBody filterBody);

    public SearchResponse fetchDropDownLists();

    public SearchResponse getTopClientIps();

    public SearchResponse getTopTrafficHours();

    public SearchResponse getTopTenSlowestApi();

    public SearchResponse getTopTenMostHittingApiWithAvgResponseTime();

    public SearchResponse getResponseTimeByServices();

    public SearchResponse getGeoLocationData();

    public SearchResponse getTraceById(String logId, String traceId, Instant timeStamp);

    public FailedJobResponse getFirstHundredGeoLocationFailedJob(String apiKey);

    public FailedJobResponse getFirstHundredElasticFailedJob(String apiKey);
}
