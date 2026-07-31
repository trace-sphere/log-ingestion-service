package com.log.ingestion.log_ingestion_service.repository;

import com.log.ingestion.log_ingestion_service.projections.*;
import com.log.ingestion.log_ingestion_service.entity.LogPrimeKey;
import com.log.ingestion.log_ingestion_service.entity.LogTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public interface LogTraceRepository extends JpaRepository<LogTrace, LogPrimeKey>, JpaSpecificationExecutor<LogTrace> {

    @Query(value = "Select DISTINCT t.service_name, h.method, h.status, t.level, t.event_type from log_trace t Left Join http_trace h  on t.trace_id=h.trace_id", nativeQuery = true)
    List<DropdownData> findAllDropDownPopulateData();

    @Query(value = "select COUNT(ht.client_ip) as ip_count, ht.client_ip  from http_trace ht GROUP BY ht.client_ip order by ip_count desc limit :dataCount", nativeQuery = true)
    List<TopClientIps> findTopClientIp(@Param("dataCount") Integer amountOfData);


    @Query(value = "select extract(hour from lt.incoming_time) as hours, COUNT(*) as request_count from log_trace lt group by extract(hour from lt.incoming_time) order by request_count desc limit 1", nativeQuery = true)
    List<PeakTrafficHours> peakTopFiveTrafficHours(Integer peakHoursFetchLimit);


    @Query(value = "select ht.path, ht.duration_ms, ht.http_id , ht.trace_id, ht.method  from http_trace ht where ht.duration_ms is not null order by duration_ms  desc limit :fetchLimit", nativeQuery = true)
    List<TopTenSlowestApi> getTopTenSlowestApi(@Param("fetchLimit") Integer fetchLimit);

    @Query(value = "select count(ht.path) as no_of_hits,round(avg(ht.duration_ms)) as avg_response_time, ht.path, ht.method  from http_trace ht where ht.path is not null and ht.path!='' group by ht.path, ht.method order by no_of_hits desc limit :dataFetchLimit", nativeQuery = true)
    List<TopTenMostHittingApi> getTopHittingApisWithAvgResponseTime(Integer dataFetchLimit);

//    @Query(value = "select count(ht.path) as hitting_time, lt.service_name from http_trace ht left join log_trace lt on ht.http_id = lt.http_trace_id  group by lt.service_name order by hitting_time desc limit :fetchLimit", nativeQuery = true)
//    List<FrequentlyHitServices> getFrequentlyHitService(Integer fetchLimit);

    @Query(value = "select count(ht.path) as no_of_hit,round(avg(ht.duration_ms)) as avg_response_time, lt.service_name from http_trace ht left join log_trace lt on ht.http_id = lt.http_trace_id where ht.duration_ms is not null group by lt.service_name order by no_of_hit desc", nativeQuery = true)
    List<AvgResponseByService> getResponseTimeByAndNoOfHitPerService();
}
