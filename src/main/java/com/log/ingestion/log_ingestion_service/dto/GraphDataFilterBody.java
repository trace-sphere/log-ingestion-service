package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class GraphDataFilterBody {
    private LocalDate firstDate;
    private int pageNo;
    private int pageSize;
}
