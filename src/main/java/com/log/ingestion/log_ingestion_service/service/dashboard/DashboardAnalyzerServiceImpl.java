package com.log.ingestion.log_ingestion_service.service.dashboard;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.MultiBucketBase;
import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.DashboardAnalyticsDto;
import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import com.log.ingestion.log_ingestion_service.enums.Events;
import com.log.ingestion.log_ingestion_service.exception.ElasticOperationException;
import com.log.ingestion.log_ingestion_service.multitenancy.TenantLocalResolverService;
import com.log.ingestion.log_ingestion_service.repository.LogElasticRepository;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardAnalyzerServiceImpl implements DashboardAnalyzerWithElasticService {

    private final LogElasticRepository logElasticRepository;
    private final MessageSource messageSource;
    private final ElasticsearchOperations elasticOperations;
    private final TenantLocalResolverService tenantResolverService;


    @Override
    public ServiceResponse saveBulk(List<LogTraceDocument> logTraceDocuments) {
        try {
            Iterable<LogTraceDocument> savedData = logElasticRepository.saveAll(logTraceDocuments);
            List<JSONObject> savedLogTraces = new ArrayList<>();
            savedData.forEach(logTraceDocument -> {
                JSONObject savedId = new JSONObject();
                savedId.put("Saved data id", logTraceDocument.getTraceId());
                savedLogTraces.add(savedId);
            });
            return ServiceResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("bulk.elastic.save.success", null, Locale.ENGLISH))
                    .details(savedLogTraces)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "saveBulk(?)");
            throw new ElasticOperationException("bulk.elastic.save.failed");
        }
    }

    @Override
    public ServiceResponse deleteAll() {
        try {
            logElasticRepository.deleteAll();
            return ServiceResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("bulk.delete.operation.success", null, Locale.ENGLISH))
                    .details(List.of())
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "deleteAll()");
            throw new ElasticOperationException("bulk.delete.operation.failed");
        }
    }

    @Override
    public SearchResponse getAll() {
        try {
            Iterable<LogTraceDocument> logTraceDocuments = logElasticRepository.findAll();
            List<LogTraceDocument> logTraceDocumentsList = new ArrayList<>();
            logTraceDocuments.forEach(logTraceDocumentsList::add);
            JSONObject response = new JSONObject();
            response.put("fetchedData", logTraceDocumentsList);
            response.put("totalDataCount", logTraceDocumentsList.size());
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("get.all.elastic.data.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception ex) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, ex.getMessage(), "getAll()");
//            throw new ElasticOperationException("get.all.elastic.data.failed");
            throw ex;
        }
    }

    @Override
    public DashboardAnalyticsDto getDashBoardAnalytics() {
        try {
            final String tenantId = tenantResolverService.getCurrentTenant();
            Query query = NativeQuery.builder().withQuery(with -> with
                            .term(ter -> ter
                                    .field("tenantId.keyword")
                                    .value(tenantId)))
                    .withMaxResults(0)

                    .withAggregation("eventCount",
                            Aggregation.of(a -> a
                                    .terms(t -> t.field("eventType"))))

                    .withAggregation("statusCount",
                            Aggregation.of(a -> a
                                    .terms(t -> t.field("status"))))

                    .withAggregation("levelCount",
                            Aggregation.of(a -> a
                                    .terms(t -> t.field("level"))))

                    .withAggregation("avgDuration",
                            Aggregation.of(a -> a
                                    .avg(avg -> avg.field("durationMs"))))

                    .withAggregation("apiRequest",
                            Aggregation.of(a -> a
                                    .filter(f -> f
                                            .term(t -> t
                                                    .field("eventType")
                                                    .value(Events.API_REQUEST.toString())))
                                    .aggregations("success",
                                            Aggregation.of(success -> success
                                                    .filter(filter -> filter
                                                            .wildcard(w -> w
                                                                    .field("status")
                                                                    .value("2*")))))))
                    .build();
            SearchHits<LogTraceDocument> searchHits =
                    elasticOperations.search(query, LogTraceDocument.class);

            ElasticsearchAggregations aggregations =
                    (ElasticsearchAggregations) searchHits.getAggregations();

            assert aggregations != null;

            DashboardAnalyticsDto dto = new DashboardAnalyticsDto();

            dto.setTotalLogs(searchHits.getTotalHits());

            dto.setEventCounts(getBucketMap(
                    aggregations.get("eventCount")));

            dto.setStatusCounts(getBucketMap(
                    aggregations.get("statusCount")));

            dto.setLevelCounts(getBucketMap(
                    aggregations.get("levelCount")));

            double avgDuration = aggregations.get("avgDuration")
                    .aggregation()
                    .getAggregate()
                    .avg()
                    .value();

            dto.setAverageResponseTime(
                    BigDecimal.valueOf(avgDuration)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue());

            Aggregate apiRequestAggregate = aggregations.get("apiRequest")
                    .aggregation()
                    .getAggregate();

            long totalApiRequests =
                    apiRequestAggregate.filter().docCount();

            long successRequests =
                    apiRequestAggregate.filter()
                            .aggregations()
                            .get("success")
                            .filter()
                            .docCount();

            double successRate = totalApiRequests == 0
                    ? 0
                    : (successRequests * 100.0) / totalApiRequests;

            dto.setSuccessRate(
                    BigDecimal.valueOf(successRate)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue());
            return dto;
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getDashBoardAnalytics()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }

    private Map<String, Long> getBucketMap(ElasticsearchAggregation aggregation) {

        return aggregation.aggregation()
                .getAggregate()
                .sterms()
                .buckets()
                .array()
                .stream()
                .collect(Collectors.toMap(
                        bucket -> bucket.key().stringValue(),
                        MultiBucketBase::docCount
                ));
    }
}
