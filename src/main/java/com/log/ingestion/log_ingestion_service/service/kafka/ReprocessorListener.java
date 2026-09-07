package com.log.ingestion.log_ingestion_service.service.kafka;

import com.log.ingestion.log_ingestion_service.entity.*;
import com.log.ingestion.log_ingestion_service.service.asynchronous.AsynchronousServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;
import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
public class ReprocessorListener {

    private final AsynchronousServices asynchronousServices;

    //@KafkaListener(topics = "geo-reprocessor-topic", groupId = "${log.geo.location.consumer.group-id}")
    public void listenGeoLocationReprocessRequest(LogRequestDto logRequestDto, Acknowledgment ack) {
        try {
            final LogPrimeKey logPrimeKey = LogPrimeKey.builder()
                    .logId(logRequestDto.getLogId())
                    .timeStamp(logRequestDto.getTimeStamp())
                    .traceId(logRequestDto.getTraceId())
                    .build();
            final String clientIp = logRequestDto.getClientIp();
            final String apiKey = logRequestDto.getApiKey();
            asynchronousServices.saveUserGeoLocation(logPrimeKey, clientIp, apiKey);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Exception at Geo location reprocessor ", e);
        }
    }

    //@KafkaListener(topics = "elastic-reprocessor-topic", groupId = "${log.elastic.consumer.group-id}")
    public void listenElasticSearchReprocessorRequest(LogRequestDto logRequestDto, Acknowledgment ack) {
        try {
            LogTrace logTrace = new LogTrace();
            final LogPrimeKey logPrimeKey = LogPrimeKey.builder()
                    .logId(logRequestDto.getLogId())
                    .timeStamp(logRequestDto.getTimeStamp())
                    .traceId(logRequestDto.getTraceId())
                    .build();
            BeanUtils.copyProperties(logRequestDto, logTrace);
            logTrace.setLogPrimaryKey(logPrimeKey);
            logTrace.setHttpTrace(buildHttpTrace(logRequestDto));
            logTrace.setExceptionTrace(buildExceptionTrace(logRequestDto));
            logTrace.setMetaDataTrace(buildMetaDataTrace(logRequestDto));
            asynchronousServices.saveDataToElasticSearch(logTrace, logRequestDto.getApiKey());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Exception at save to elastic search reprocessor ", e);
        }
    }

    private HttpTrace buildHttpTrace(final LogRequestDto logRequestDto) {
        return HttpTrace.builder()
                .traceId(logRequestDto.getTraceId())
                .clientIp(logRequestDto.getClientIp())
                .durationMs(logRequestDto.getDurationMs())
                .method(logRequestDto.getMethod())
                .path(logRequestDto.getPath())
                .status(logRequestDto.getStatus())
                .userAgent(logRequestDto.getUserAgent())
                .build();
    }

    private ExceptionTrace buildExceptionTrace(final LogRequestDto logRequestDto) {
        return ExceptionTrace.builder()
                .traceId(logRequestDto.getTraceId())
                .exceptionClass(logRequestDto.getExceptionClass())
                .stackTrace(logRequestDto.getStackTrace())
                .exceptionMessage(logRequestDto.getExceptionMessage())
                .build();
    }

    private MetaDataTrace buildMetaDataTrace(LogRequestDto logRequestDto) {
        return MetaDataTrace.builder()
                .region(logRequestDto.getRegion())
                .timeZone(logRequestDto.getTimeZone())
                .version(logRequestDto.getVersion())
                .build();
    }
}
