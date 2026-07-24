package com.log.ingestion.log_ingestion_service.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.MultiBucketBase;
import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import com.log.ingestion.log_ingestion_service.enums.Events;
import com.log.ingestion.log_ingestion_service.exception.ElasticOperationException;
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
        try{
            logElasticRepository.deleteAll();
            return ServiceResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("bulk.delete.operation.success", null, Locale.ENGLISH))
                    .details(List.of())
                    .build();
        } catch(Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "deleteAll()");
            throw new ElasticOperationException("bulk.delete.operation.failed");
        }
    }

    @Override
    public SearchResponse getAll() {
        try{
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
    public JSONObject getTotalRequestCountByEventType() {
        try {
            Query query = NativeQuery.builder()
                    .withAggregation("eventCount",
                            Aggregation.of(agr ->
                                    agr.terms(term ->
                                            term.field("eventType"))))
                    .build();
            SearchHits<LogTraceDocument> searchHits = elasticOperations.search(query, LogTraceDocument.class);
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            assert aggregations != null;
            ElasticsearchAggregation aggregation = aggregations.aggregations().iterator().next();
            Map<String, Long> eventCountResponses = aggregation.aggregation()
                    .getAggregate()
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(bucket -> bucket.key().stringValue(), MultiBucketBase::docCount));
            return new JSONObject(eventCountResponses);
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getTotalRequestByEventType()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }

    @Override
    public double getSuccessRateInRequests() {
        try {
            Query query = NativeQuery.builder()
                    .withMaxResults(0)
                    .withQuery(quer -> quer.term(ter -> ter.field("eventType").value(Events.API_REQUEST.toString())))
                    .withAggregation("statusCount", Aggregation.of(agr -> agr
                            .filter(fil -> fil
                                    .wildcard(wild -> wild
                                            .field("status")
                                            .value("2*")
                                    ))))
                    .build();
            SearchHits<LogTraceDocument> searchHits = elasticOperations.search(query, LogTraceDocument.class);
            long totalCount = searchHits.getTotalHits();
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            ElasticsearchAggregation aggregation = aggregations.get("statusCount");
            assert aggregation != null;
            long successCount = aggregation.aggregation().getAggregate().filter().docCount();
            double successRate = (Double) (double) successCount / (double) totalCount * 100;
            successRate = BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
            return successRate;
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getSuccessRateInRequests()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }

    @Override
    public double getAvgResponseTimeOfApplication() {
        try {
            Query query = NativeQuery.builder()
                    .withMaxResults(0)
                    .withAggregation("averageResponseTime",
                            Aggregation.of(agr -> agr
                                    .avg(a -> a.
                                            field("durationMs"))))
                    .build();
            SearchHits<LogTraceDocument> searchHits = elasticOperations.search(query, LogTraceDocument.class);

            ElasticsearchAggregations aggregations =
                    (ElasticsearchAggregations) searchHits.getAggregations();
            ElasticsearchAggregation aggregation = aggregations.get("averageResponseTime");
            assert aggregation != null;
            Aggregate aggregate = aggregation.aggregation().getAggregate();
            double averageDuration = aggregate.avg().value();
            averageDuration = BigDecimal.valueOf(averageDuration).setScale(2, RoundingMode.HALF_UP).doubleValue();
            return averageDuration;
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getSuccessRateInRequests()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }

    @Override
    public long getDataCount() {
        try {
            Query query = NativeQuery.builder().build();
            return elasticOperations.count(query, LogTraceDocument.class);
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getSuccessRateInRequests()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }

    @Override
    public JSONObject getStatusTypeCount() {
        try {
            Query query = NativeQuery.builder()
                    .withAggregation("statusType",
                            Aggregation.of(agr ->
                                    agr.terms(term ->
                                            term.field("status"))))
                    .build();
            SearchHits<LogTraceDocument> searchHits = elasticOperations.search(query, LogTraceDocument.class);
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            assert aggregations != null;
            ElasticsearchAggregation aggregation = aggregations.aggregations().iterator().next();
            Map<String, Long> statusCountResponses = aggregation.aggregation()
                    .getAggregate()
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(bucket -> bucket.key().stringValue(), MultiBucketBase::docCount));
             return new JSONObject(statusCountResponses);
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getTotalRequestByEventType()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }

    @Override
    public JSONObject getLevelTypeCount() {
        try {
            Query query = NativeQuery.builder()
                    .withAggregation("levelCount",
                            Aggregation.of(agr ->
                                    agr.terms(term ->
                                            term.field("level"))))
                    .build();
            SearchHits<LogTraceDocument> searchHits = elasticOperations.search(query, LogTraceDocument.class);
            ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
            assert aggregations != null;
            ElasticsearchAggregation aggregation = aggregations.aggregations().iterator().next();
            Map<String, Long> levelCountResponses = aggregation.aggregation()
                    .getAggregate()
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .collect(Collectors.toMap(bucket -> bucket.key().stringValue(), MultiBucketBase::docCount));
            return new JSONObject(levelCountResponses);
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getTotalRequestByEventType()");
            throw new ElasticOperationException("dashboard.analyze.event.failed");
        }
    }
}
