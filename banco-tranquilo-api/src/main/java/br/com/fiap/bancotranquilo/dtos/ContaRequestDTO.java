package br.com.fiap.bancotranquilo.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ContaRequestDTO(
        @NotBlank(message = "Informe o número da conta") String numeroConta,
        @NotBlank(message = "Informe o titular") String titular,
        @NotBlank(message = "Informe o CPF") String cpf,
        @Email(message = "E-mail inválido") String email,
        @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo") BigDecimal saldoInicial
) {
}
