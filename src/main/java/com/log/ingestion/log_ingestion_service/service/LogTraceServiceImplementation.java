package com.log.ingestion.log_ingestion_service.service;

import com.log.ingestion.log_ingestion_service.config.TenantContext;
import com.log.ingestion.log_ingestion_service.config.UserNameContext;
import com.log.ingestion.log_ingestion_service.dto.*;
import com.log.ingestion.log_ingestion_service.entity.*;
import com.log.ingestion.log_ingestion_service.enums.Events;
import com.log.ingestion.log_ingestion_service.enums.OperationStatus;
import com.log.ingestion.log_ingestion_service.exception.ConditionalValidatorException;
import com.log.ingestion.log_ingestion_service.exception.DataFetchException;
import com.log.ingestion.log_ingestion_service.exception.LogCreationException;
import com.log.ingestion.log_ingestion_service.exception.SearchSpecException;
import com.log.ingestion.log_ingestion_service.projections.*;
import com.log.ingestion.log_ingestion_service.repository.LogTraceRepository;
import com.log.ingestion.log_ingestion_service.repository.UserGeoCoordinateRepository;
import com.log.ingestion.log_ingestion_service.service.asynchronous.AsynchronousServices;
import com.log.ingestion.log_ingestion_service.service.dashboard.DashboardAnalyzerWithElasticService;
import com.log.ingestion.log_ingestion_service.service.kafka.KafkaProducerService;
import com.log.ingestion.log_ingestion_service.specification.LogInventorySpecService;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import com.log.ingestion.log_ingestion_service.validators.RequestConditionalValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class LogTraceServiceImplementation implements LogTraceService {

    private final LogTraceRepository logTraceRepository;
    private final MessageSource messageSource;
    private final RequestConditionalValidatorService conditionalValidator;
    private final LogInventorySpecService logInventorySpecService;
    private final AsynchronousServices asynchronousService;
    private final UserGeoCoordinateRepository geoCoordinateRepository;
    private final DashboardAnalyzerWithElasticService analyzerService;
    private final KafkaProducerService kafkaProducerService;
    private final ApiKeyService apiKeyService;

    @Value("${initial.analyze.data.fetch.limit}")
    private Integer fetchLimit;

    @Value("${number.peak.hours.data.fetch.limit}")
    private Integer peakHoursFetchLimit;

    @Override
    public ServiceResponse saveLog(LogRequestDto logRequestDto) {
        String traceId = UUID.randomUUID().toString();
        logRequestDto.setTraceId(traceId);
        setTenant(logRequestDto.getApiKey());
        LogPrimeKey logPrimeKey = LogPrimeKey.builder()
                .logId(UUID.randomUUID().toString())
                .traceId(traceId)
                .timeStamp(logRequestDto.getTimeStamp())
                .build();
        Optional<LogTrace> logTraceOptional = logTraceRepository.findById(logPrimeKey);
        if (logTraceOptional.isPresent()) {
            throw new LogCreationException(messageSource.getMessage("log.creation.duplicate.log", null, Locale.ENGLISH));
        }
        try {
            validateEventType(logRequestDto.getEventType(), logRequestDto);
            HttpTrace httpTrace = HttpTrace.builder()
                    .traceId(traceId)
                    .clientIp(logRequestDto.getClientIp())
                    .durationMs(logRequestDto.getDurationMs())
                    .method(logRequestDto.getMethod())
                    .path(logRequestDto.getPath())
                    .status(logRequestDto.getStatus())
                    .userAgent(logRequestDto.getUserAgent())
                    .build();
            MetaDataTrace metaDataTrace = MetaDataTrace.builder()
                    .region(logRequestDto.getRegion())
                    .timeZone(logRequestDto.getTimeZone())
                    .version(LogConstants.CURRENT_LOG_VERSION)
                    .build();
            ExceptionTrace exceptionTrace = ExceptionTrace.builder()
                    .traceId(traceId)
                    .exceptionClass(logRequestDto.getExceptionClass())
                    .stackTrace(logRequestDto.getStackTrace())
                    .exceptionMessage(logRequestDto.getExceptionMessage())
                    .build();
            LogTrace logTrace = new LogTrace();
            BeanUtils.copyProperties(logRequestDto, logTrace);
            logTrace.setMetaDataTrace(metaDataTrace);
            logTrace.setLogPrimaryKey(logPrimeKey);
            logTrace.setIncomingTime(LocalTime.now());
            logTrace.setHttpTrace(httpTrace);
            logTrace.setExceptionTrace(exceptionTrace);
            logTrace.setElasticOperationStatus(OperationStatus.PENDING);
            logTrace.setGeoLocationOperationStatus(OperationStatus.PENDING);
            LogTrace savedLogTrace = logTraceRepository.saveAndFlush(logTrace);
            asynchronousService.saveUserGeoLocation(
                    savedLogTrace.getLogPrimaryKey(), logRequestDto.getClientIp(), logRequestDto.getApiKey());
            asynchronousService.saveDataToElasticSearch(savedLogTrace, logRequestDto.getApiKey());
            kafkaProducerService.sendNotification(logRequestDto);
            return ServiceResponse.builder()
                    .error(false)
                    .message("Log saved successfully !")
                    .details(List.of())
                    .build();
        } catch (ConditionalValidatorException e) {
            log.info(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.toString(), " method() saveLog->");
            throw new ConditionalValidatorException(e.getMessage());
        } catch (Exception e) {
            log.info("Failed for message: {}" + " method() saveLog->", e.getMessage());
            asynchronousService.takeBackUpWithRollBack(logRequestDto);
            throw new LogCreationException("log.creation.failed.msg");
        }
    }

    private void validateEventType(Events eventType, LogRequestDto requestDto) {
        try {
            switch (eventType.toString()) {
                case "EXCEPTION":
                    ExceptionTraceValidator exceptionValidator = new ExceptionTraceValidator();
                    BeanUtils.copyProperties(requestDto, exceptionValidator);
                    conditionalValidator.validateEventType(exceptionValidator);
                    break;
                case "APPLICATION_LOG":
                    log.info("Application log, safely saving..!");
                    break;
                case "API_REQUEST":
                    HttpTraceValidator httpTraceValidator = new HttpTraceValidator();
                    BeanUtils.copyProperties(requestDto, httpTraceValidator);
                    conditionalValidator.validateHttpEventType(httpTraceValidator);
                    break;
                case "CUSTOM_EVENT":
                    log.info("Custom event, safely saving..!");
                    break;
                default:
                    log.info("Default event, safely saving..!");
            }
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX + " method() ValidEventType", e.getMessage());
            throw new ConditionalValidatorException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse searchBySpec(SearchRequestDto requestBody, int pageNo, int pageSize) {
        try {
            Specification<LogTrace> logTraceSpecification = logInventorySpecService.logTracePredicateBuilder(requestBody);
            Pageable pageRequest = PageRequest.of(pageNo, pageSize);
            Page<LogTrace> logPage = logTraceRepository.findAll(logTraceSpecification, pageRequest);
            Long totalFetchedDataCount = logPage.stream().count();
            Integer numberOfPages = logPage.getTotalPages();
            long totalDataCount = logTraceRepository.count(logTraceSpecification);
            List<LogTrace> logTraceList = logPage.stream().toList();
            Map<String, Long> httpStatusMap = logTraceList.stream().collect(Collectors
                    .groupingBy(
                            logTrace -> logTrace.getHttpTrace().getStatus(),
                            Collectors.counting()));
            Map<String, Long> eventTypeMap = logTraceList.stream().collect(Collectors.groupingBy(
                    logTrace -> logTrace.getEventType().toString(),
                    Collectors.counting()));
            Map<String, Long> levelTypeMap = logTraceList.stream().collect(
                    Collectors.groupingBy(logTrace -> logTrace.getLevel().toString(),
                            Collectors.counting())
            );
            Integer avgRequestDuration = logTraceList.stream()
                    .collect(Collectors.averagingInt(log -> {
                        if (log.getHttpTrace().getDurationMs() == null) {
                            return 0;
                        } else {
                            return log.getHttpTrace().getDurationMs();
                        }
                    })).intValue();
            long noOfSuccessRate = logTraceList.stream().filter(log -> {
                        if (log.getHttpTrace().getStatus() != null) {
                            return log.getHttpTrace().getStatus().startsWith("2");
                        } else {
                            return false;
                        }
                    })
                    .count();
            double successRate = 0;
            if (!(noOfSuccessRate == 0 || totalDataCount == 0)) {
                successRate = (double) noOfSuccessRate / (double) totalDataCount * 100;
            }
            successRate = BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
            JSONObject response = new JSONObject();
            JSONObject requestPerformance = new JSONObject();
            requestPerformance.put("averageDurationMs", avgRequestDuration);
            requestPerformance.put("successRate", successRate);
            response.put("totalFetchedDataCount", totalFetchedDataCount);
            response.put("totalDataCount", totalDataCount);
            response.put("numberOfPages", numberOfPages);
            response.put("responsePageSize", pageSize);

            List<JSONObject> responseList = new ArrayList<>();
            logPage.forEach(logTraceData -> {
                JSONObject logData = new JSONObject();
                logData.put("id", logTraceData.getLogPrimaryKey().getLogId());
                logData.put("timeStamp", logTraceData.getLogPrimaryKey().getTimeStamp());
                logData.put("serviceName", logTraceData.getServiceName());
                logData.put("environment", logTraceData.getEnvironment());
                logData.put("host", logTraceData.getHost());
                logData.put("instanceId", logTraceData.getInstanceId());
                logData.put("eventType", logTraceData.getEventType());
                logData.put("level", logTraceData.getLevel());
                logData.put("thread", logTraceData.getThread());
                logData.put("logger", logTraceData.getLogger());
                logData.put("message", logTraceData.getMessage());
                logData.put("traceId", logTraceData.getLogPrimaryKey().getTraceId());
                logData.put("spanId", logTraceData.getSpanId());
                logData.put("method", logTraceData.getHttpTrace().getMethod());
                logData.put("path", logTraceData.getHttpTrace().getPath());
                logData.put("status", logTraceData.getHttpTrace().getStatus());
                logData.put("durationMs", logTraceData.getHttpTrace().getDurationMs());
                logData.put("clientIp", logTraceData.getHttpTrace().getClientIp());
                logData.put("userAgent", logTraceData.getHttpTrace().getUserAgent());
                logData.put("exceptionClass", logTraceData.getExceptionTrace().getExceptionClass());
                logData.put("exceptionMessage", logTraceData.getExceptionTrace().getExceptionMessage());
                logData.put("stackTrace", logTraceData.getExceptionTrace().getStackTrace());
                responseList.add(logData);
            });
            response.put("data", responseList);
            response.put("httpStatusMetaData", new JSONObject(httpStatusMap));
            response.put("eventTypeMetaData", new JSONObject(eventTypeMap));
            response.put("levelMetaData", new JSONObject(levelTypeMap));
            response.put("requestStates", requestPerformance);
            return new SearchResponse().toBuilder()
                    .error(false)
                    .message(messageSource.getMessage("log.search.success.msg", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "searchBySpec()");
            throw new SearchSpecException("log.search.fail.message");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getAllCardData() {
        log.info("Entering into get card data");
        try {
            DashboardAnalyticsDto analyzedData = analyzerService.getDashBoardAnalytics();
            JSONObject response = new JSONObject();
            response.put("analyzedData", analyzedData);
            log.info("Leaving from get card data");
            return new SearchResponse().toBuilder()
                    .error(false)
                    .message(messageSource.getMessage("log.search.success.msg", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getCardData()");
            throw new SearchSpecException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceResponse getGraphData(GraphDataFilterBody filterBody) {
        LocalDate startDate = filterBody.getFirstDate();
        if (startDate == null) {
            filterBody.setFirstDate(LocalDate.now());
        }
        if (Objects.requireNonNull(startDate).isAfter(LocalDate.now())) {
            throw new SearchSpecException("future.date.fetch.invalid");
        }
        try {
            Specification<LogTrace> logTraceSpecification = logInventorySpecService.logGraphPredicateBuilder(filterBody);
            int pageNo = filterBody.getPageNo();
            int pageSize = filterBody.getPageSize();
            Pageable page = PageRequest.of(pageNo, pageSize);
            Page<LogTrace> logTracePage = logTraceRepository.findAll(logTraceSpecification, page);
            TreeMap<LocalDateTime, Long> graphData = logTracePage.get()
                    .collect(Collectors.groupingBy(logTrace ->
                                    logTrace.getLogPrimaryKey()
                                            .getTimeStamp()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime()
                                            .truncatedTo(ChronoUnit.MINUTES),
                            TreeMap::new,
                            Collectors.counting()));

            List<JSONObject> graphDataList = graphData.entrySet().stream().map(
                    entry -> {
                        JSONObject graphDataJson = new JSONObject();
                        graphDataJson.put(entry.getKey(), entry.getValue());
                        return graphDataJson;
                    }
            ).collect(Collectors.toList());
            graphDataList.sort(Comparator.comparing(jsonObject ->
                    (LocalDateTime) jsonObject.keySet().iterator().next()
            ));
            return new ServiceResponse().toBuilder()
                    .error(false)
                    .message(messageSource.getMessage("graph.data.fetched.success", null, Locale.ENGLISH))
                    .details(graphDataList)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage() + e.getMessage(), "getGraphData()");
            throw new SearchSpecException("graph.data.fetched.fail");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse fetchDropDownLists() {
        try {
            List<DropdownData> dropDownList = logTraceRepository.findAllDropDownPopulateData();
            List<DropDown> serviceNameMap = dropDownList.stream().map(DropdownData::getServiceName)
                    .distinct()
                    .filter(Objects::nonNull)
                    .filter(serviceName -> !Objects.equals(serviceName, ""))
                    .map(service -> new DropDown(service.toUpperCase(), service)).toList();

            List<DropDown> levelMap = dropDownList.stream().map(DropdownData::getLevel)
                    .distinct()
                    .filter(Objects::nonNull)
                    .filter(level -> !Objects.equals(level, ""))
                    .map(level -> new DropDown(level.toUpperCase(), level)).toList();

            List<DropDown> methodMap = dropDownList.stream().map(DropdownData::getMethod)
                    .distinct()
                    .filter(Objects::nonNull)
                    .filter(method -> !Objects.equals(method, ""))
                    .map(method -> new DropDown(method.toUpperCase(), method)).toList();

            List<DropDown> statusMap = dropDownList.stream().map(DropdownData::getStatus)
                    .distinct()
                    .filter(Objects::nonNull)
                    .filter(status -> !Objects.equals(status, ""))
                    .map(status -> new DropDown(status, status)).toList();

            List<DropDown> eventMap = dropDownList.stream().map(DropdownData::getEventType)
                    .distinct()
                    .filter(Objects::nonNull)
                    .filter(event -> !Objects.equals(event, ""))
                    .map(event -> new DropDown(event.toUpperCase(), event)).toList();
            JSONObject response = new JSONObject();
            response.put("serviceDropDown", serviceNameMap);
            response.put("levelDropDown", levelMap);
            response.put("methodDropDown", methodMap);
            response.put("statusDropDown", statusMap);
            response.put("eventDropDown", eventMap);

            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.drop.down.fetch.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "fetchDropDownLists()");
            throw new SearchSpecException("data.drop.down.fetch.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getTopClientIps() {
        try {
            List<TopClientIps> topClientIps = logTraceRepository.findTopClientIp(fetchLimit);
            Long fetchedDataCount = (long) topClientIps.size();
            JSONObject response = new JSONObject();
            response.put("topClientIps", topClientIps);
            response.put("fetchedDataCount", fetchedDataCount);
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.top.ip.fetch.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "topClientIps()");
            throw new SearchSpecException("data.top.ip.fetch.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getTopTrafficHours() {
        try {
            List<PeakTrafficHours> peakTrafficHours = logTraceRepository.peakTopFiveTrafficHours(peakHoursFetchLimit);
            Long fetchedDataCount = (long) peakTrafficHours.size();
            Map<String, Long> modifiedPeakTrafficMap = peakTrafficHours.stream()
                    .collect(Collectors.toMap(
                            traffic -> {
                                if (traffic.getHours() == null)
                                    return "24:00 - 01:00";
                                return (traffic.getHours() != 24) ?
                                        traffic.getHours() +
                                                ":00" +
                                                " - " +
                                                (traffic.getHours() + 1) +
                                                ":00" : "24:00 - 01:00";
                            },
                            PeakTrafficHours::getRequestCount
                    ));
            JSONObject response = new JSONObject();
            response.put("peakTrafficHours", new JSONObject(modifiedPeakTrafficMap));
            response.put("fetchedDataCount", fetchedDataCount);
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.peak.traffic.fetch.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "getTopTrafficHours()");
            throw new SearchSpecException("data.peak.traffic.fetch.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getTopTenSlowestApi() {
        try {
            List<TopTenSlowestApi> topSlowestApi = logTraceRepository.getTopTenSlowestApi(fetchLimit);
            JSONObject response = new JSONObject();
            response.put("data", topSlowestApi);
            response.put("fetchedDataCount", topSlowestApi.size());
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.slowest.api.fetch.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "getTopTenSlowestApi()");
            throw new SearchSpecException("data.slowest.api.fetch.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getTopTenMostHittingApiWithAvgResponseTime() {
        try {
            List<TopTenMostHittingApi> mostHittingApis = logTraceRepository.getTopHittingApisWithAvgResponseTime(fetchLimit);
            JSONObject response = new JSONObject();
            response.put("data", mostHittingApis);
            response.put("fetchedDataSize", mostHittingApis.size());
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.most.hitting.api.fetch.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "getTopTenMostHittingApi()");
            throw new SearchSpecException("data.most.hitting.api.fetch.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getResponseTimeByServices() {
        try {
            List<AvgResponseByService> avgResponseByServices = logTraceRepository.getResponseTimeByAndNoOfHitPerService();
            JSONObject response = new JSONObject();
            response.put("data", avgResponseByServices);
            response.put("fetchedDataSize", avgResponseByServices.size());
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.most.avg.service.response.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "mostHittingServices()");
            throw new SearchSpecException("data.most.avg.service.response.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getGeoLocationData() {
        try {
            List<GeoLocationData> geoLocationData = geoCoordinateRepository.getGeoLocationData();
            JSONObject response = new JSONObject();
            response.put("data", geoLocationData);
            response.put("fetchedDataSize", geoLocationData.size());
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("data.geo.location.response.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e, "mostHittingServices()");
            throw new SearchSpecException("data.geo.location.response.failed");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public SearchResponse getTraceById(String logId, String traceId, Instant timeStamp) {
        LogPrimeKey primeKey = LogPrimeKey.builder()
                .traceId(traceId)
                .timeStamp(timeStamp)
                .logId(logId)
                .build();
        Optional<LogTrace> logTraceOptional = logTraceRepository.findById(primeKey);
        if (logTraceOptional.isEmpty()) {
            throw new DataFetchException("fetch.trace.data.failed");
        }
        try {
            LogTrace logTraceData = logTraceOptional.get();
            JSONObject logData = new JSONObject();
            logData.put("id", logTraceData.getLogPrimaryKey().getLogId());
            logData.put("timeStamp", logTraceData.getLogPrimaryKey().getTimeStamp());
            logData.put("serviceName", logTraceData.getServiceName());
            logData.put("environment", logTraceData.getEnvironment());
            logData.put("host", logTraceData.getHost());
            logData.put("instanceId", logTraceData.getInstanceId());
            logData.put("eventType", logTraceData.getEventType());
            logData.put("level", logTraceData.getLevel());
            logData.put("thread", logTraceData.getThread());
            logData.put("logger", logTraceData.getLogger());
            logData.put("message", logTraceData.getMessage());
            logData.put("traceId", logTraceData.getLogPrimaryKey().getTraceId());
            logData.put("spanId", logTraceData.getSpanId());
            logData.put("method", logTraceData.getHttpTrace().getMethod());
            logData.put("path", logTraceData.getHttpTrace().getPath());
            logData.put("status", logTraceData.getHttpTrace().getStatus());
            logData.put("durationMs", logTraceData.getHttpTrace().getDurationMs());
            logData.put("clientIp", logTraceData.getHttpTrace().getClientIp());
            logData.put("userAgent", logTraceData.getHttpTrace().getUserAgent());
            logData.put("exceptionClass", logTraceData.getExceptionTrace().getExceptionClass());
            logData.put("exceptionMessage", logTraceData.getExceptionTrace().getExceptionMessage());
            logData.put("stackTrace", logTraceData.getExceptionTrace().getStackTrace());
            JSONObject response = new JSONObject();
            response.put("traceData", logData);
            return SearchResponse.builder()
                    .error(false)
                    .message(messageSource.getMessage("fetch.trace.data.success", null, Locale.ENGLISH))
                    .results(response)
                    .build();
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.EXCEPTION_PREFIX, e.getMessage(), "getTraceById(PrimaryKeyDto logPrimeKeyDto)");
            throw e;
        }
    }


    @Override
    public FailedJobResponse getFirstHundredGeoLocationFailedJob(String apiKey) {
        System.out.println("___________ controller"+ apiKey);
        setTenant(apiKey);
        try {
            List<LogTrace> failedGeoLocationTrace = logTraceRepository.getFirstHundredGeoLocationAccordingToStatus(OperationStatus.FAILED.toString());
            if (failedGeoLocationTrace.isEmpty()) {
                return FailedJobResponse.builder()
                        .error(false)
                        .result(Collections.emptyList())
                        .build();
            }
            ArrayList<LogRequestDto> logTraceList = new ArrayList<>();
            for (LogTrace logTrace : failedGeoLocationTrace) {
                LogRequestDto logRequestDto = buildLogRequestDtoFromLogTrace(logTrace);
                logTraceList.add(logRequestDto);
            }
            return FailedJobResponse.builder()
                    .error(false)
                    .result(logTraceList)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch failed job", e);
            throw e;
        }
    }

    @Override
    public FailedJobResponse getFirstHundredElasticFailedJob(String apiKey) {
        setTenant(apiKey);
        try {
            List<LogTrace> failedGeoLocationTrace = logTraceRepository.getFirstHundredElasticDocumentAccordingToStatus(OperationStatus.FAILED.toString());
            if (failedGeoLocationTrace.isEmpty()) {
                return FailedJobResponse.builder()
                        .error(false)
                        .result(Collections.emptyList())
                        .build();
            }
            ArrayList<LogRequestDto> logTraceList = new ArrayList<>();
            for (LogTrace logTrace : failedGeoLocationTrace) {
                LogRequestDto logRequestDto = buildLogRequestDtoFromLogTrace(logTrace);
                logTraceList.add(logRequestDto);
            }

            return FailedJobResponse.builder()
                    .error(false)
                    .result(logTraceList)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch failed job", e);
            throw e;
        }
    }

    private LogRequestDto buildLogRequestDtoFromLogTrace(LogTrace logTrace) {
        LogRequestDto logRequestDto = new LogRequestDto();
        BeanUtils.copyProperties(logTrace, logRequestDto);
        HttpTrace httpTrace = logTrace.getHttpTrace();
        if (httpTrace != null) {
            logRequestDto.toBuilder()
                    .clientIp(httpTrace.getClientIp())
                    .durationMs(httpTrace.getDurationMs())
                    .method(httpTrace.getMethod())
                    .path(httpTrace.getPath())
                    .status(httpTrace.getStatus())
                    .userAgent(httpTrace.getUserAgent())
                    .build();
        }
        ExceptionTrace exceptionTrace = logTrace.getExceptionTrace();
        if (exceptionTrace != null) {
            logRequestDto.toBuilder()
                    .exceptionClass(exceptionTrace.getExceptionClass())
                    .exceptionId(exceptionTrace.getExceptionId())
                    .exceptionMessage(exceptionTrace.getExceptionMessage())
                    .stackTrace(exceptionTrace.getStackTrace())
                    .build();
        }
        MetaDataTrace metaDataTrace = logTrace.getMetaDataTrace();
        if (metaDataTrace != null) {
            logRequestDto.toBuilder()
                    .region(metaDataTrace.getRegion())
                    .timeZone(metaDataTrace.getTimeZone())
                    .version(metaDataTrace.getVersion())
                    .build();
        }
        LogPrimeKey logPrimeKey = logTrace.getLogPrimaryKey();
        if (logPrimeKey != null) {
            logRequestDto.toBuilder()
                    .logId(logPrimeKey.getLogId())
                    .traceId(logPrimeKey.getTraceId())
                    .timeStamp(logPrimeKey.getTimeStamp())
                    .build();
        }
        return logRequestDto;
    }

    private void setTenant(String apiKey) {
        System.out.println("___________ controller"+ apiKey);
        UserApiKeyResponseDto apiKeyDetails = apiKeyService.getTenantDetails(apiKey);
        TenantContext.currentTenant.set(apiKeyDetails.getTenantId());
        UserNameContext.currentUser.set(apiKeyDetails.getUserName());
    }
}
