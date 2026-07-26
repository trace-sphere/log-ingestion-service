package com.log.ingestion.log_ingestion_service.controller;

import com.log.ingestion.log_ingestion_service.dto.*;
import com.log.ingestion.log_ingestion_service.service.LogTraceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RequiredArgsConstructor
@RestController
@RequestMapping("/logTrace")
@CrossOrigin(value = "http://localhost:4300")
public class LogTraceController {

    private final LogTraceService logtraceService;

    @PostMapping("/save")
    public ResponseEntity<ServiceResponse> save(@Valid @RequestBody LogRequestDto requestBody) {
        ServiceResponse serviceResponse = logtraceService.saveLog(requestBody);
        return new ResponseEntity<>(serviceResponse, HttpStatus.OK);
    }

    @PostMapping("/getBySearch")
    public ResponseEntity<SearchResponse> getLogBySpec(@RequestBody SearchRequestDto requestBody, @RequestParam int pageNo, @RequestParam int pageSize) {
        SearchResponse searchResponse = logtraceService.searchBySpec(requestBody, pageNo, pageSize);
        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
    }

    @GetMapping("/getCardData")
    public ResponseEntity<SearchResponse> getAllCardData() {
        SearchResponse response = logtraceService.getAllCardData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/fetchGraphData")
    public ResponseEntity<ServiceResponse> getGraphData(@RequestBody GraphDataFilterBody filterBody) {
        ServiceResponse response = logtraceService.getGraphData(filterBody);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getDropDown/data")
    public ResponseEntity<SearchResponse> getDropDown() {
        SearchResponse response = logtraceService.fetchDropDownLists();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/top/clientIp")
    public ResponseEntity<SearchResponse> getTopClientIp() {
        SearchResponse response = logtraceService.getTopClientIps();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/peak/traffics")
    public ResponseEntity<SearchResponse> getPeakTrafficHours() {
        SearchResponse response = logtraceService.getTopTrafficHours();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/apis/slowest")
    public ResponseEntity<SearchResponse> getTopTenSlowApi() {
        SearchResponse response = logtraceService.getTopTenSlowestApi();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/apis/mostHitting")
    public ResponseEntity<SearchResponse> getMostHittingWithAvgResponseTime() {
        SearchResponse response = logtraceService.getTopTenMostHittingApiWithAvgResponseTime();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/avg/service")
    public ResponseEntity<SearchResponse> avgResponseTimeByServices() {
        SearchResponse response = logtraceService.getResponseTimeByServices();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/geo/coordinates")
    public ResponseEntity<SearchResponse> getGeoLocationData() {
        SearchResponse response = logtraceService.getGeoLocationData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/fetchTrace")
    public ResponseEntity<SearchResponse> fetchTrace(@RequestParam String logId, @RequestParam String traceId, @RequestParam Instant timeStamp) {
        SearchResponse response = logtraceService.getTraceById(logId, traceId, timeStamp);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
