package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;
import org.json.simple.JSONObject;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class SearchResponse {
    private boolean error;
    private String message;
    private JSONObject results;
}
