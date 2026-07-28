package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.entity.HttpTrace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HttpTraceRepository extends JpaRepository<HttpTrace, String> {
}
