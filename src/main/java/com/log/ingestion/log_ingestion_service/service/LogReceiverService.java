package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LogReceiverService {

    private final LogTraceService logTraceService;

    @KafkaListener(topics = "LogIngestion", groupId = "log-consumer-group")
    public void kafkaMessageReaderMessageResolver(LogRequestDto logRequestDto, Acknowledgment ack) {
        log.info("Entering into save mode, at kafka Message receiver");
        ServiceResponse response = logTraceService.saveLog(logRequestDto);
        if(!response.isError()) {
            ack.acknowledge();
        } else {
            log.error("Save failed, some thing went wrong");
        }
    }

    @KafkaListener(topics = "LogIngestion-dlt")
    public void dltConsumer(String message, Acknowledgment ack) {
        System.out.println("Failed message" + message);
        ack.acknowledge();
    }

}
