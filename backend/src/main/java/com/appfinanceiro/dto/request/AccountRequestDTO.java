package com.appfinanceiro.dto.request;

import com.appfinanceiro.domain.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequestDTO(
    @NotBlank(message = "O nome da conta é obrigatório.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    String name,

    @NotNull(message = "O tipo de conta é obrigatório.")
    AccountType type,

    @NotNull(message = "O saldo inicial é obrigatório.")
    BigDecimal initialBalance,

    String bankName,
    String color,
    String icon
) {}
