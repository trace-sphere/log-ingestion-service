package com.log.ingestion.log_ingestion_service.config;

import com.log.ingestion.log_ingestion_service.dto.UserApiKeyResponseDto;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "api-key-service", url = "http://localhost:9069", path = "/authentication-service")
public interface FeignClientService {

    @GetMapping("/userAuth/api/apiKeyDetails/{apiKey}")
    public UserApiKeyResponseDto getApiKeyDetails(@PathVariable String apiKey,
                                                  @RequestHeader(LogConstants.CustomHeader.X_ACCESS_KEY) String accessKey);
}
