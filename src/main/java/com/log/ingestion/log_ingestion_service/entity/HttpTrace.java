package com.log.ingestion.log_ingestion_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
@Entity
@Table(name = "HTTP_TRACE")
public class HttpTrace {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String httpId;
    private String traceId;
    private String method;
    private String path;
    private String status;
    private Integer durationMs;
    private String clientIp;
    private String userAgent;
}
