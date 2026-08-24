package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.entity.LogBackUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogBackUpRepository extends JpaRepository<LogBackUp, String> {

}
