package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.entity.ExceptionTrace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExceptionTraceRepository extends JpaRepository<ExceptionTrace, String> {
}
