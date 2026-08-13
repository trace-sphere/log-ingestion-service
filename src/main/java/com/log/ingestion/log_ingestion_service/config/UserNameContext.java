package com.log.ingestion.log_ingestion_service.config;

public class UserNameContext {
    public static final ThreadLocal<String> currentUser = new ThreadLocal<>();
}
