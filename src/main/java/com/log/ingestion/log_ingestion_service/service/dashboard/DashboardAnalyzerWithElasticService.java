package com.log.ingestion.log_ingestion_service.service.dashboard;

import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.DashboardAnalyticsDto;
import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;

import java.util.List;


public interface DashboardAnalyzerWithElasticService {

    public ServiceResponse saveBulk(List<LogTraceDocument> logTraceDocuments);

    public DashboardAnalyticsDto getDashBoardAnalytics();

    public ServiceResponse deleteAll();

    public SearchResponse getAll();

    public SearchResponse searchByApiPathStackTraceExceptionType(String searchKeyWord);

}
