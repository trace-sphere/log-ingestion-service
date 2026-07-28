package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.config.FeignClientService;
import com.log.ingestion.log_ingestion_service.config.TenantContext;
import com.log.ingestion.log_ingestion_service.dto.UserApiKeyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApiKeyService {

    private final FeignClientService feignClientService;

    @Cacheable(cacheNames = "tenant-cache", key = "#apiKey")
    public UserApiKeyResponseDto getTenantDetails(String apiKey) {
        return feignClientService.getApiKeyDetails(apiKey);
    }
}
