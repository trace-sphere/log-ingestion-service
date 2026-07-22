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
@Table(name = "USER_GEO_COORDINATE")
public class UserGeoCoordinate {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
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
    private Long trafficCount;
}
