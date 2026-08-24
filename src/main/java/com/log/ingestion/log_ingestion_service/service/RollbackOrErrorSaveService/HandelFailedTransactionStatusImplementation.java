package com.log.ingestion.log_ingestion_service.service.RollbackOrErrorSaveService;

import com.log.ingestion.log_ingestion_service.enums.OperationStatus;
import com.log.ingestion.log_ingestion_service.repository.LogTraceRepository;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class HandelFailedTransactionStatusImplementation implements HandelFailedTransactionAndStatus {

    private final LogTraceRepository logTraceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateFailedStatusForElasticOperation(String traceId) {
        try {
            int result = logTraceRepository.updateElasticOperationStatus(traceId, LogConstants.STATUS.FAILED);
            log.info("{} no of row updated", result);
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.PREFIX, e.getMessage(), "updateFailedStatusForElasticOperation(?, ?)");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateFailedStatusForGeoLocation(String traceId) {
        try {
            int result = logTraceRepository.updateTraceGeoLocationStatus(traceId, LogConstants.STATUS.FAILED);
            log.info("{} no of row updated", result);
        } catch (Exception e) {
            log.error(LogConstants.ExceptionMsg.PREFIX, e.getMessage(), "updateFailedStatusForElasticOperation(?, ?)");
        }
    }

}
