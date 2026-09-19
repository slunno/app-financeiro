package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
    UUID id,
    String title,
    String message,
    NotificationType type,
    Boolean isRead,
    UUID referenceId,
    LocalDateTime createdAt
) {}
