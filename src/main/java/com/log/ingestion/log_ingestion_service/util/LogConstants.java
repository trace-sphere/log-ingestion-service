package com.log.ingestion.log_ingestion_service.util;

public class LogConstants {
    public static final String CURRENT_LOG_VERSION = "1.0.0";
    public static final String DEFAULT_USER_ROLE = "GUEST";
    public static final String DEFAULT_EVENT_TYPE ="Custom Event";

    public static class ExceptionMsg {
        public static final String EXCEPTION_PREFIX = "Exception occurred for message: {} at method {}";
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
}
