package br.com.fiap.bancotranquilo.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoDTO(
        @NotNull(message = "Informe o valor") @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero") BigDecimal valor
) {
}
