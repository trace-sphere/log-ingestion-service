package com.log.ingestion.log_ingestion_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import com.log.ingestion.log_ingestion_service.entity.UserGeoCoordinate;
import com.log.ingestion.log_ingestion_service.exception.LogCreationException;
import com.log.ingestion.log_ingestion_service.exception.SearchSpecException;
import com.log.ingestion.log_ingestion_service.repository.LogTraceRepository;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncServiceImplementation implements AsynchronousServices {

    private final LogTraceRepository logTraceRepository;
    private final MessageSource messageSource;
    private final ExternalRequestService externalRequestService;

    @Transactional
    @Async
    @Override
    public void saveUserGeoLocation(LogPrimeKey logPrimeKey, String clientIp) {
        log.info("Entering in to save geo location service");
        Optional<LogTrace> logTraceOptional = logTraceRepository.findById(logPrimeKey);
        if (logTraceOptional.isEmpty()) {
            log.info(messageSource.getMessage("geo.location.failed.msg", null, Locale.ENGLISH));
            throw new LogCreationException("geo.location.failed.msg");
        }
        try {
            LogTrace logTrace = logTraceOptional.get();
            UserGeoCoordinate userGeoCoordinate = buildUserGeoCoordinate(clientIp);
            userGeoCoordinate.setTraceId(logPrimeKey.getTraceId());
            logTrace.setUserGeoCoordinate(userGeoCoordinate);
            logTraceRepository.save(logTrace);
            log.info("Log details with geo location data saved successfully");
        } catch (Exception e) {
            log.info(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "saveGeoLocationDetails(LogPrimeKey logPrimeKey)");
            throw new LogCreationException("log.creation.failed.msg");
        }
    }

    private UserGeoCoordinate buildUserGeoCoordinate(String clientIp) {
        try {
            JSONObject ipStackResponse = externalRequestService.getUserIpDetails(clientIp);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject timeZone = mapper.convertValue(ipStackResponse.get("time_zone"), JSONObject.class);
            return UserGeoCoordinate.builder()
                    .clientIp(clientIp)
                    .city(ipStackResponse.get("city").toString())
                    .country(ipStackResponse.get("country_name").toString())
                    .continentCode(ipStackResponse.get("continent_code").toString())
                    .countryCode(ipStackResponse.get("country_code").toString())
                    .continentName(ipStackResponse.get("continent_name").toString())
                    .region(ipStackResponse.get("region_name").toString())
                    .latitude(ipStackResponse.get("latitude").toString())
                    .longitude(ipStackResponse.get("longitude").toString())
                    .zipCode(ipStackResponse.get("zip").toString())
                    .timeZone(timeZone.get("id").toString())
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "buildUserGeoCoordinate(?)");
            throw new SearchSpecException("log.creation.failed.msg");
        }
    }
}
