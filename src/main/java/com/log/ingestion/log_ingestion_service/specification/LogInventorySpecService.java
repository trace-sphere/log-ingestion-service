package com.log.ingestion.log_ingestion_service.specification;

import com.log.ingestion.log_ingestion_service.dto.GraphDataFilterBody;
import com.log.ingestion.log_ingestion_service.dto.SearchRequestDto;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import org.springframework.data.jpa.domain.Specification;

public interface LogInventorySpecService {
    Specification<LogTrace> logTracePredicateBuilder(SearchRequestDto requestBody);
    Specification<LogTrace> logGraphPredicateBuilder(GraphDataFilterBody filterBody);
}
