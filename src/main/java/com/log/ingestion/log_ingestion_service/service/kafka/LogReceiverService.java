package com.log.ingestion.log_ingestion_service.service.kafka;

import com.log.ingestion.log_ingestion_service.config.TenantContext;
import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;
import com.log.ingestion.log_ingestion_service.dto.ServiceResponse;
import com.log.ingestion.log_ingestion_service.dto.UserApiKeyResponseDto;
import com.log.ingestion.log_ingestion_service.service.ApiKeyService;
import com.log.ingestion.log_ingestion_service.service.LogTraceService;
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
    private final ApiKeyService apiKeyService;

    @KafkaListener(topics = "LogIngestion", groupId = "log-consumer-group")
    public void kafkaMessageReaderMessageResolver(LogRequestDto logRequestDto, Acknowledgment ack) {
        log.info("Entering into save mode, at kafka Message receiver");
        boolean isSet = setTenant(logRequestDto.getApiKey());
        if(isSet) {
            ServiceResponse response = logTraceService.saveLog(logRequestDto);
            TenantContext.currentTenant.remove();
            if(!response.isError()) {
                ack.acknowledge();
        }
        } else {
            log.error("Save failed, some thing went wrong");
        }
    }

    @KafkaListener(topics = "LogIngestion-dlt")
    public void dltConsumer(String message, Acknowledgment ack) {
        try {
            System.out.println("Failed message" + message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error(e.getMessage()+ "at ingestion dlt");
        }
    }

    private boolean setTenant(String apiKey) {
        UserApiKeyResponseDto apiKeyDetails = apiKeyService.getTenantDetails(apiKey);
        TenantContext.currentTenant.set(apiKeyDetails.getTenantId());
        return TenantContext.currentTenant.get() != null;
    }

}
