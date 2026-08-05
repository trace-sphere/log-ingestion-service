package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
public class GlobalSearchResponse {
    private String logId;
    private String timeStamp;
    private String traceId;
    private String resultant;
}
