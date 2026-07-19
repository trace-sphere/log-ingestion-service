package com.log.ingestion.log_ingestion_service.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
public class UserGeoDetailDto {

    private String geoLocationId;
    private String clientIp;
    private String region;
    private String country;
    private String city;
    private String latitude;
    private String longitude;
    private String traceId;
    private String zipCode;
    private String timeZone;
    private String countryCode;
    private String continentName;
    private String continentCode;
}
