package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import org.json.simple.JSONObject;

import java.util.List;


public interface DashboardAnalyzerWithElasticService {

    public ServiceResponse saveBulk(List<LogTraceDocument> logTraceDocuments);

    public JSONObject getTotalRequestCountByEventType();

    public double getSuccessRateInRequests();

    public double getAvgResponseTimeOfApplication();

    public long getDataCount();

    public JSONObject getStatusTypeCount();

    public JSONObject getLevelTypeCount();

    public ServiceResponse deleteAll();

    public SearchResponse getAll();

}
