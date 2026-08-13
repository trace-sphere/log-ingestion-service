package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class MessageBodyDto {
    private String targetUser;
    private String message;
    private Long updatedDataCount;
    private String logType;
}
