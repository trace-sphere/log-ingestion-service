package com.log.ingestion.log_ingestion_service.service.kafka;

import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;

public interface KafkaProducerService {

    public void sendNotification(LogRequestDto logRequestDto);

}
