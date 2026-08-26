package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.entity.UserGeoCoordinate;
import com.log.ingestion.log_ingestion_service.projections.GeoLocationData;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;
import org.hibernate.annotations.OptimisticLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserGeoCoordinateRepository extends JpaRepository<UserGeoCoordinate, String> {

    List<UserGeoCoordinate> findByClientIp(String clientIp);

    @Transactional
    @Modifying
    @Query(value = "update user_geo_coordinate set traffic_count=traffic_count+1 where client_ip=:clientIp", nativeQuery = true)
    public int increaseTrafficCountInUserGeoCoordinate(@Param("clientIp") String clientIp);

    @Query(value = "select sum(ugc.traffic_count) as app_traffic_count, ugc.client_ip, ugc.longitude, ugc.latitude, ugc.city from public.user_geo_coordinate ugc group by ugc.client_ip, ugc.latitude, ugc.longitude, ugc.city", nativeQuery = true)
    List<GeoLocationData> getGeoLocationData();


    @Modifying
    @Query(value = "INSERT INTO user_geo_coordinate (geo_location_id, city, client_ip, continent_code, continent_name, country, country_code, latitude, longitude, region, time_zone, trace_id, zip_code, traffic_count) VALUES(:geoLocationId, :city, :clientIp, :continentCode, :continentName, :country, :countryCode, :latitude, :longitude, :region, :timeZone, :traceId, :zipCode, 1) on conflict(client_ip) do update set traffic_count = user_geo_coordinate.traffic_count + 1;", nativeQuery = true)
    public int saveGeoCoordinateAndTrafficCount(@Param("geoLocationId") String geoLocationId, @Param("city") String city, @Param("clientIp") String clientIp, @Param("continentCode") String continentCode, @Param("continentName") String continentName, @Param("country") String country, @Param("countryCode") String countryCode, @Param("latitude") String latitude, @Param("longitude") String longitude, @Param("region") String region, @Param("timeZone") String timeZone, @Param("traceId") String traceId, @Param("zipCode") String zipCode);
}
