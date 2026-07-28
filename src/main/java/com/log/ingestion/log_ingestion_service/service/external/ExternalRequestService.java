package com.log.ingestion.log_ingestion_service.service.external;

import org.json.simple.JSONObject;

public interface ExternalRequestService {
    public JSONObject getUserIpDetails(String userIp);
}
