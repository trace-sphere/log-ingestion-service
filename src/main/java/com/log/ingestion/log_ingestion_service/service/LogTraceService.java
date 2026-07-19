package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.dto.*;

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
}
