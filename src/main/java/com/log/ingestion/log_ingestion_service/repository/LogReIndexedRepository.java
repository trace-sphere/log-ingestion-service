package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.document.LogTraceReIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogReIndexedRepository extends ElasticsearchRepository<LogTraceReIndex, String> {
}
