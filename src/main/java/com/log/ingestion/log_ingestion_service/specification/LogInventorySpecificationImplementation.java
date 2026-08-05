package com.log.ingestion.log_ingestion_service.specification;

import com.log.ingestion.log_ingestion_service.dto.GraphDataFilterBody;
import com.log.ingestion.log_ingestion_service.dto.SearchRequestDto;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;

@Slf4j
@Service
public class LogInventorySpecificationImplementation implements LogInventorySpecService {


    public Specification<LogTrace> logTracePredicateBuilder(SearchRequestDto requestBody) {
        return (Root<LogTrace> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            Timestamp firstTimeStamp = null;
            Timestamp lastTimeStamp = null;
            if (requestBody.getFirstTimeStamp() != null && requestBody.getLastTimeStamp() != null) {
                firstTimeStamp = Timestamp.valueOf(requestBody.getFirstTimeStamp());
                lastTimeStamp = Timestamp.valueOf(requestBody.getLastTimeStamp());
            }
            Predicate finalPredicate = criteriaBuilder.conjunction();
            if (requestBody.getStatus() != null && !requestBody.getStatus().isEmpty()) {
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("httpTrace").get("status"), requestBody.getStatus()));
            }
            if (firstTimeStamp != null && !requestBody.getFirstTimeStamp().isEmpty()) {
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.between(root.get("id").get("timeStamp"), firstTimeStamp, lastTimeStamp));
            }

            if (requestBody.getEventType() != null && !requestBody.getEventType().isEmpty()) {
                String eventType = requestBody.getEventType();
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("eventType"), eventType));
            }

            if (requestBody.getMethod() != null && !requestBody.getMethod().isEmpty()) {
                String method = requestBody.getMethod();
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("httpTrace").get("method"), method));
            }

            if (requestBody.getServiceName() != null && !requestBody.getServiceName().isEmpty()) {
                String service = requestBody.getServiceName();
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("serviceName"), service));
            }

            if (requestBody.getLevel() != null && !requestBody.getLevel().isEmpty()) {
                String level = requestBody.getLevel();
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("level"), level));
            }


            if (requestBody.getDurationMs() != null) {
                Integer duration = requestBody.getDurationMs();
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("httpTrace").get("durationMs"), duration));
            }

            if(requestBody.getTraceId()!=null && !requestBody.getTraceId().isBlank()) {
                String traceId = requestBody.getTraceId();
                finalPredicate = criteriaBuilder.and(finalPredicate, criteriaBuilder.equal(root.get("id").get("traceId"), traceId));
            }

            query.orderBy(criteriaBuilder.asc(root.get("httpTrace").get("durationMs")));

            return finalPredicate;
        };
    }

    @Override
    public Specification<LogTrace> logGraphPredicateBuilder(GraphDataFilterBody filterBody) {

        return (root, query, criteriaBuilder) -> {

            Predicate finalPredicate = criteriaBuilder.conjunction();

            if (filterBody.getFirstDate() != null ) {
                ZoneId zone = ZoneId.systemDefault();
                Instant startingTime = filterBody.getFirstDate().atStartOfDay(zone).toInstant();
                Instant endingTime = filterBody.getFirstDate().atTime(LocalTime.MAX).atZone(zone).toInstant();
                Predicate dataOfRequestedRangeOfDaysPredicate = criteriaBuilder
                        .between(root.get("id").get("timeStamp"), startingTime, endingTime);
                finalPredicate = finalPredicate == null ?
                        dataOfRequestedRangeOfDaysPredicate
                        : criteriaBuilder.and(finalPredicate, dataOfRequestedRangeOfDaysPredicate);
            }
            assert query != null;
            query.orderBy(criteriaBuilder.asc(root.get("id").get("timeStamp")));
            return finalPredicate;
        };
    }
}
