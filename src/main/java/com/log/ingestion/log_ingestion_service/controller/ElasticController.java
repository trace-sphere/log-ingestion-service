package com.log.ingestion.log_ingestion_service.controller;

import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import com.log.ingestion.log_ingestion_service.service.DashboardAnalyzerWithElasticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class ElasticController {

    private final DashboardAnalyzerWithElasticService analyzerService;

    @PostMapping("/saveBulk")
    public ResponseEntity<ServiceResponse> testBulkSave(@RequestBody List<LogTraceDocument> bulkLogTrace) {
        ServiceResponse response = analyzerService.saveBulk(bulkLogTrace);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
