package com.log.ingestion.log_ingestion_service.controller;

import com.log.ingestion.log_ingestion_service.dto.FailedJobResponse;
import com.log.ingestion.log_ingestion_service.dto.SearchResponse;
import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import com.log.ingestion.log_ingestion_service.service.LogTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/retry")
public class InternalServiceEndPoint {

    private final LogTraceService logTraceService;

    //@GetMapping("/failedGeoLocation")
    public ResponseEntity<FailedJobResponse> triggerAsyncGeoLocation(@RequestParam String apiKey) {
        System.out.println("___________ controller"+ apiKey);
        FailedJobResponse response = logTraceService.getFirstHundredGeoLocationFailedJob(apiKey);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //@GetMapping("/failedElasticDocument")
    public ResponseEntity<FailedJobResponse> triggerAsyncElasticDocument(@RequestParam String apiKey) {
        System.out.println("___________ controller"+ apiKey);
        FailedJobResponse response = logTraceService.getFirstHundredElasticFailedJob(apiKey);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
