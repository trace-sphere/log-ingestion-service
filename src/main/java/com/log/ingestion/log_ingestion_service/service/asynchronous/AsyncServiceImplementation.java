package com.log.ingestion.log_ingestion_service.service.asynchronous;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.ingestion.log_ingestion_service.config.TenantContext;
import com.log.ingestion.log_ingestion_service.document.LogTraceDocument;
import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;
import com.log.ingestion.log_ingestion_service.dto.UserApiKeyResponseDto;
import com.log.ingestion.log_ingestion_service.entity.LogBackUp;
import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import com.log.ingestion.log_ingestion_service.entity.UserGeoCoordinate;
import com.log.ingestion.log_ingestion_service.enums.OperationStatus;
import com.log.ingestion.log_ingestion_service.exception.LogCreationException;
import com.log.ingestion.log_ingestion_service.exception.SearchSpecException;
import com.log.ingestion.log_ingestion_service.repository.LogBackUpRepository;
import com.log.ingestion.log_ingestion_service.repository.LogElasticRepository;
import com.log.ingestion.log_ingestion_service.repository.LogTraceRepository;
import com.log.ingestion.log_ingestion_service.repository.UserGeoCoordinateRepository;
import com.log.ingestion.log_ingestion_service.service.ApiKeyService;
import com.log.ingestion.log_ingestion_service.service.RollbackOrErrorSaveService.HandelFailedTransactionAndStatus;
import com.log.ingestion.log_ingestion_service.service.external.ExternalRequestService;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AsyncServiceImplementation implements AsynchronousServices {

    private final LogTraceRepository logTraceRepository;
    private final MessageSource messageSource;
    private final ExternalRequestService externalRequestService;
    private final UserGeoCoordinateRepository geoCoordinateRepository;
    private final LogElasticRepository logElasticRepository;
    private final ApiKeyService apiKeyService;
    private final LogBackUpRepository logBackUpRepository;
    private final HandelFailedTransactionAndStatus handelFailedTransactionAndStatus;

    private final Object geoLock = new Object();

    @Async
    @Override
    public void saveUserGeoLocation(LogPrimeKey logPrimeKey, String clientIp, String apiKey) {
        log.info("Entering in to save geo location service");
        setTenant(apiKey);
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Returned because api key is null or blank at geo location save operation");
            return;
        }
        if (clientIp == null || clientIp.isEmpty()) {
            log.info("Returned because ip is null or blank");
            return;
        }
        Optional<LogTrace> logTraceOptional = logTraceRepository.findById(logPrimeKey);
        if (logTraceOptional.isEmpty()) {
            log.info(messageSource.getMessage("geo.location.failed.msg", null, Locale.ENGLISH));
            throw new LogCreationException("geo.location.failed.msg");
        }
        LogTrace logTrace = logTraceOptional.get();
        if(!logTrace.getGeoLocationOperationStatus().equals(OperationStatus.PENDING)) {
            log.info("geo location already processing or complete");
            throw new LogCreationException("geo.location.failed.msg");
        }
        try {
            synchronized (geoLock) {
                List<UserGeoCoordinate> geoCoordinateList = geoCoordinateRepository.findByClientIp(clientIp);
                if (!geoCoordinateList.isEmpty()) {
                    int rowCount = geoCoordinateRepository.increaseTrafficCountInUserGeoCoordinate(clientIp);
                    if (rowCount > 0) {
                        log.info("Traffic count recorded");
                        logTraceRepository
                                .updateTraceGeoLocationStatus(
                                        logTrace.getLogPrimaryKey().getTraceId(), LogConstants.STATUS.COMPLETE);
                        return;
                    }
                }
                UserGeoCoordinate userGeoCoordinate = buildUserGeoCoordinate(clientIp);
                userGeoCoordinate.setTraceId(logPrimeKey.getTraceId());
                logTrace.setUserGeoCoordinate(userGeoCoordinate);
                logTraceRepository.save(logTrace);
            }
            logTraceRepository
                    .updateTraceGeoLocationStatus(logTrace.getLogPrimaryKey().getTraceId(), LogConstants.STATUS.COMPLETE);
            log.info("Log details with geo location data saved successfully");
        } catch (Exception e) {
            log.info(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "saveGeoLocationDetails(LogPrimeKey logPrimeKey)");
            throw new LogCreationException("log.creation.failed.msg");
        } finally {
            TenantContext.currentTenant.remove();
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

    @Async
    @Override
    public void saveDataToElasticSearch(LogTrace logTrace, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Returned because api key is null or blank at elastic save operation");
            return;
        }
        setTenant(apiKey);
        if (!logTrace.getElasticOperationStatus().equals(OperationStatus.PENDING)) {
            log.info("Process stopped because operation status is not pending at saveDataToElasticSearch");
            return;
        }
        logTraceRepository.
                updateElasticOperationStatus(logTrace.getLogPrimaryKey().getTraceId(), LogConstants.STATUS.PROCESSED);
        try {
            LogTraceDocument logDocument = buildLogTraceDocument(logTrace);
            String currentTenant = TenantContext.currentTenant.get();
            currentTenant = (currentTenant == null || currentTenant.isBlank()) ? apiKey : currentTenant;
            logDocument.setTenantId(currentTenant);
            logElasticRepository.save(logDocument);
            logTraceRepository
                    .updateElasticOperationStatus(logTrace.getLogPrimaryKey().getTraceId(), LogConstants.STATUS.COMPLETE);
            log.info("Log document data saved successfully in elastic search...!");
        } catch (Exception ex) {
            handelFailedTransactionAndStatus.updateFailedStatusForElasticOperation(logTrace.getLogPrimaryKey().getTraceId());
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, ex.getMessage(), "saveDataToElasticSearch(?)");
            throw ex;
        }
    }

    private void setTenant(String apiKey) {
        UserApiKeyResponseDto apiKeyDetails = apiKeyService.getTenantDetails(apiKey);
        TenantContext.currentTenant.set(apiKeyDetails.getTenantId());
    }

    public LogTraceDocument buildLogTraceDocument(LogTrace logRequest) {
        try {
            return LogTraceDocument.builder()
                    .traceId(logRequest.getLogPrimaryKey().getTraceId())
                    .exceptionClass(blankToNull(logRequest.getExceptionTrace().getExceptionClass()))
                    .host(logRequest.getHost())
                    .level(Objects.toString(logRequest.getLevel(), null))
                    .exceptionMessage(logRequest.getExceptionTrace().getExceptionMessage())
                    .message(logRequest.getMessage())
                    .path(logRequest.getHttpTrace().getPath())
                    .durationMs(logRequest.getHttpTrace().getDurationMs())
                    .stackTrace(logRequest.getExceptionTrace().getStackTrace())
                    .environment(blankToNull(logRequest.getEnvironment()))
                    .eventType(Objects.toString(logRequest.getEventType(), null))
                    .incomingTime(LocalTime.now())
                    .instanceId(logRequest.getInstanceId())
                    .logger(logRequest.getLogger())
                    .serviceName(logRequest.getServiceName())
                    .timeStamp(logRequest.getLogPrimaryKey().getTimeStamp() == null ? null : LocalDateTime.ofInstant(logRequest.getLogPrimaryKey().getTimeStamp(), ZoneId.of("Asia/Kolkata")))
                    .clientIp(blankToNull(logRequest.getHttpTrace().getClientIp()))
                    .method(blankToNull(logRequest.getHttpTrace().getMethod()))
                    .status(blankToNull(logRequest.getHttpTrace().getStatus()))
                    .thread(logRequest.getThread())
                    .userAgent(logRequest.getHttpTrace().getUserAgent())
                    .version(logRequest.getMetaDataTrace().getVersion())
                    .build();
        } catch (Exception ex) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, ex.getMessage(), "buildLogTraceDocumentDto(LogTrace logTrace)");
            throw ex;
        }
    }

    @Override
    public void takeBackUpWithRollBack(LogRequestDto logRequestDto) {
        if (logRequestDto.getApiKey() == null) {
            log.info("Unable to take back up, invalid client credential");
            throw new LogCreationException("Log save failed, invalid client credential");
        }
        try {
            log.info("Entering into log failed backup");
            LogBackUp logBackUp = new LogBackUp();
            BeanUtils.copyProperties(logRequestDto, logBackUp);
            logBackUp.setOperationStatus(LogConstants.STATUS.PENDING);
            logBackUpRepository.save(logBackUp);
            log.info("Failed log backup taken successfully");
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "takeBackUpWithRollBack(LogRequestDto logRequestDto)");
        }
    }

    public String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
