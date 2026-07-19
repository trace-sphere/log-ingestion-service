package com.log.ingestion.log_ingestion_service.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Embeddable
public class LogPrimeKey {
    private String logId;
    private Instant timeStamp;
    private String traceId;
}
