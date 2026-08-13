package com.log.ingestion.log_ingestion_service.service.kafka;

import com.log.ingestion.log_ingestion_service.config.TenantContext;
import com.log.ingestion.log_ingestion_service.config.UserNameContext;
import com.log.ingestion.log_ingestion_service.dto.LogRequestDto;
import com.log.ingestion.log_ingestion_service.dto.MessageBodyDto;
import com.log.ingestion.log_ingestion_service.exception.LogAuthenticationException;
import com.log.ingestion.log_ingestion_service.util.LogConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaMessageSenderImplementation implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MessageSource messageSource;

    @Override
    public void sendNotification(LogRequestDto logRequestDto) {
        log.info("Entered into send notification producer");
        MessageBodyDto messageBodyDto = MessageBodyDto.builder()
                .targetUser(getTargetUser())
                .logType(Objects.toString(logRequestDto.getEventType(), LogConstants.DEFAULT_EVENT_TYPE))
                .updatedDataCount(1L)
                .message(messageSource.getMessage("message.new.data.available", null, Locale.ENGLISH))
                .build();
        kafkaTemplate.send(LogConstants.KafkaConstant.PRODUCER_TOPIC, messageBodyDto);
        log.info("Leaving from send notification producer");
    }

    private String getTargetUser() {
        log.info("Entering into get target user");
        final String userName = UserNameContext.currentUser.get();
        UserNameContext.currentUser.remove();
        TenantContext.currentTenant.remove();
        return userName;
    }
}
