package com.log.ingestion.log_ingestion_service.util;

public class LogConstants {
    public static final String CURRENT_LOG_VERSION = "1.0.0";
    public static final String DEFAULT_USER_ROLE = "GUEST";
    public static final String DEFAULT_EVENT_TYPE = "Custom Event";

    public static class ExceptionMsg {
        public static final String EXCEPTION_PREFIX = "Exception occurred for message: {} at method {}";
        public static final String PREFIX = "Exception occurred for message: {} at {}";
    }

    public static class SCHEMAS {
        public static final String DEFAULT_SCHEMA = "public";
    }

    public static class CustomHeader {
        public static final String X_ACCESS_KEY = "x-access-key";
    }

    public static class KafkaConstant {
        public static final String RECEIVER_TOPIC = "LogIngestion";
        public static final String PRODUCER_TOPIC = "trace-notification";
    }

    public static class STATUS {
        public static final String PENDING = "PENDING";
        public static final String COMPLETE = "COMPLETE";
        public static final String DISCARD = "DISCARDED";
        public static final String PROCESSED = "PROCESSING";
        public static final String FAILED = "FAILED";
        public static final String MAX_RETRY_FAILED = "MAXIMUM_RETRY_FAILED";
    }

    public static class SERVICE_ROLE {
        public static final String INTERNAL_SCHEDULER = "internal-scheduler-service";
    }
}
