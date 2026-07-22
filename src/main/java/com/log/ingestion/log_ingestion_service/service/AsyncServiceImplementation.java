package com.log.ingestion.log_ingestion_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import com.log.ingestion.log_ingestion_service.entity.UserGeoCoordinate;
import com.log.ingestion.log_ingestion_service.exception.LogCreationException;
import com.log.ingestion.log_ingestion_service.exception.SearchSpecException;
import com.log.ingestion.log_ingestion_service.repository.LogTraceRepository;
import com.log.ingestion.log_ingestion_service.repository.UserGeoCoordinateRepository;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncServiceImplementation implements AsynchronousServices {

    private final LogTraceRepository logTraceRepository;
    private final MessageSource messageSource;
    private final ExternalRequestService externalRequestService;
    private final UserGeoCoordinateRepository geoCoordinateRepository;

    private final Object geoLock = new Object();

    @Async
    @Override
    public void saveUserGeoLocation(LogPrimeKey logPrimeKey, String clientIp) {
        log.info("Entering in to save geo location service");
        if (clientIp == null || clientIp.isEmpty()) {
            log.info("Returned because ip is null or blank");
            return;
        }
        Optional<LogTrace> logTraceOptional = logTraceRepository.findById(logPrimeKey);
        if (logTraceOptional.isEmpty()) {
            log.info(messageSource.getMessage("geo.location.failed.msg", null, Locale.ENGLISH));
            throw new LogCreationException("geo.location.failed.msg");
        }
        try {
            synchronized (geoLock) {
                List<UserGeoCoordinate> geoCoordinateList = geoCoordinateRepository.findByClientIp(clientIp);
                if (!geoCoordinateList.isEmpty()) {
                    int rowCount = geoCoordinateRepository.increaseTrafficCountInUserGeoCoordinate(clientIp);
                    if (rowCount > 0) {
                        log.info("Traffic count recorded");
                        return;
                    }
                }
                UserGeoCoordinate userGeoCoordinate = buildUserGeoCoordinate(clientIp);
                LogTrace logTrace = logTraceOptional.get();
                userGeoCoordinate.setTraceId(logPrimeKey.getTraceId());
                logTrace.setUserGeoCoordinate(userGeoCoordinate);
                logTraceRepository.save(logTrace);
            }
            log.info("Log details with geo location data saved successfully");
        } catch (Exception e) {
            log.info(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "saveGeoLocationDetails(LogPrimeKey logPrimeKey)");
            throw new LogCreationException("log.creation.failed.msg");
        }
    }

    private UserGeoCoordinate buildUserGeoCoordinate(String clientIp) {
        log.info("Entering in to buildUserGeoCoordinate() method");
        try {
            JSONObject ipStackResponse = externalRequestService.getUserIpDetails(clientIp);
            ObjectMapper mapper = new ObjectMapper();
            JSONObject timeZone = mapper.convertValue(ipStackResponse.get("time_zone"), JSONObject.class);
            log.info("Leaving from buildUserGeoCoordinate() method");
            return UserGeoCoordinate.builder()
                    .clientIp(clientIp)
                    .city(String.valueOf(ipStackResponse.get("city")))
                    .country(String.valueOf(ipStackResponse.get("country_name")))
                    .continentCode(String.valueOf(ipStackResponse.get("continent_code")))
                    .countryCode(String.valueOf(ipStackResponse.get("country_code")))
                    .continentName(String.valueOf(ipStackResponse.get("continent_name")))
                    .region(String.valueOf(ipStackResponse.get("region_name")))
                    .latitude(String.valueOf(ipStackResponse.get("latitude")))
                    .longitude(String.valueOf(ipStackResponse.get("longitude")))
                    .zipCode(String.valueOf(ipStackResponse.get("zip")))
                    .timeZone(String.valueOf(timeZone.get("id")))
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "buildUserGeoCoordinate(?)");
            throw new SearchSpecException("log.creation.failed.msg");
        }
    }
}
