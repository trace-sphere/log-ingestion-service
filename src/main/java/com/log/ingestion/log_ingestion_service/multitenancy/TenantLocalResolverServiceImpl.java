package com.log.ingestion.log_ingestion_service.multitenancy;

import com.log.ingestion.log_ingestion_service.exception.LogAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class TenantLocalResolverServiceImpl implements TenantLocalResolverService {

    @Override
    public String getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new LogAuthenticationException("Unable to resolve current tenant contact admin");
        }
        Jwt jwt = jwtAuthenticationToken.getToken();
        final String tenantId = jwt.getClaimAsString("LOG-TENANT-ID");
        if (tenantId == null || tenantId.isBlank()) {
            throw new LogAuthenticationException("Unable to resolve current tenant contact admin");
        }
        return tenantId;
    }
}
