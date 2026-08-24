package com.log.ingestion.log_ingestion_service.service.RollbackOrErrorSaveService;

import com.log.ingestion.log_ingestion_service.enums.OperationStatus;

public interface HandelFailedTransactionAndStatus {

    public void updateFailedStatusForElasticOperation(String traceId);
    public void updateFailedStatusForGeoLocation(String traceId);

}
