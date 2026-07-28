package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@ToString
public class UserApiKeyResponseDto {
    private String tenantId;
    private String apiKey;
    private String userName;
    private String email;
}
