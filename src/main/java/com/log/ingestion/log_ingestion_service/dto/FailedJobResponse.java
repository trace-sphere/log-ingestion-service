package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FailedJobResponse {
    private boolean error;
    private List<LogRequestDto> result;
}
