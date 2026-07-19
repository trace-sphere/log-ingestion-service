package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.exception.ExternalApiRequestException;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExternalRequestServiceImpl implements ExternalRequestService{

    private final RestTemplate restTemplate;

    @Value("${log.user.ip.stack.base.uri}")
    String ipStackUriBaseUri;

    @Value("${log.user.ip.stack.uri.access.key}")
    String ipStackAccessKey;

    @Override
    public JSONObject getUserIpDetails(String clientIp) {
        try{
            String ipStackUri = UriComponentsBuilder
                    .fromUriString(ipStackUriBaseUri)
                    .queryParam("access_key", ipStackAccessKey)
                    .buildAndExpand(clientIp).toUriString();
            JSONObject response = restTemplate.getForObject(ipStackUri, JSONObject.class);
            return response;
        } catch (Exception ex) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, ex, "getUserIpDetails(?)");
            throw new ExternalApiRequestException("abstract.error.details");
        }
    }
}
