package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogElasticRepository extends ElasticsearchRepository<LogTraceDocument, String> {
}
