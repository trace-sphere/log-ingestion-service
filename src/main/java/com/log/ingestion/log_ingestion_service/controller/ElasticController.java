package com.log.ingestion.log_ingestion_service.controller;

import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import com.log.ingestion.log_ingestion_service.service.dashboard.DashboardAnalyzerWithElasticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/deleteBulk")
    public ResponseEntity<ServiceResponse> deleteData() {
        ServiceResponse response = analyzerService.deleteAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<SearchResponse> getAll() {
        SearchResponse response = analyzerService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
