package com.log.ingestion.log_ingestion_service.multitenancy;

import com.log.ingestion.log_ingestion_service.config.TenantContext;
import com.log.ingestion.log_ingestion_service.exception.LogAuthenticationException;
import com.log.ingestion.log_ingestion_service.service.ApiKeyService;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import lombok.RequiredArgsConstructor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.currentTenant.get();
        if (!(tenantId == null || tenantId.isBlank())) {
            return tenantId;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return LogConstants.SCHEMAS.DEFAULT_SCHEMA;
        }
        Jwt jwt = jwtAuthenticationToken.getToken();
        tenantId = jwt.getClaimAsString("LOG-TENANT-ID");
        if (tenantId == null || tenantId.isBlank()) {
            throw new LogAuthenticationException("Invalid grant, can not resolve tenant");
        }
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
