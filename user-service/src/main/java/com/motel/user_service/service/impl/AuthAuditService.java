package com.motel.user_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motel.user_service.entity.AuthAuditLog;
import com.motel.user_service.repository.AuthAuditLogRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sharing.enums.user_service.AuditEventType;
import sharing.enums.user_service.AuditResult;

@Service
public class AuthAuditService {
    private final AuthAuditLogRepository authAuditLogRepository;
    private final ObjectMapper objectMapper;

    public AuthAuditService(AuthAuditLogRepository authAuditLogRepository, ObjectMapper objectMapper) {
        this.authAuditLogRepository = authAuditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void log(
            String requestId,
            UUID actorUserId,
            UUID targetUserId,
            AuditEventType eventType,
            AuditResult result,
            Map<String, Object> metadata) {
        AuthAuditLog log = new AuthAuditLog();
        log.setRequestId(requestId);
        log.setActorUserId(actorUserId);
        log.setTargetUserId(targetUserId);
        log.setEventType(eventType);
        log.setResult(result);
        log.setMetadata(toJson(metadata));
        authAuditLogRepository.save(log);
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize metadata", ex);
        }
    }
}
