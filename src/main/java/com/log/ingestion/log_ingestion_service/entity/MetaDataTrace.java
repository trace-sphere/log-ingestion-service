package com.log.ingestion.log_ingestion_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@Embeddable
public class MetaDataTrace {
    private String version;
    private String region;
    private String timeZone;
}
