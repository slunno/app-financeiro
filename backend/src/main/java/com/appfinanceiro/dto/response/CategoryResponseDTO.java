package com.appfinanceiro.dto.response;

import com.appfinanceiro.domain.enums.TransactionType;

import java.util.UUID;

public record CategoryResponseDTO(
    UUID id,
    String name,
    String icon,
    String color,
    TransactionType type,
    Boolean isSystemDefault
) {}
