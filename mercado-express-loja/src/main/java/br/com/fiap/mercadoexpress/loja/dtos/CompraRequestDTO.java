package br.com.fiap.mercadoexpress.loja.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompraRequestDTO(
        @NotBlank(message = "Informe o nome do cliente") String clienteNome,
        @NotBlank(message = "Informe o e-mail do cliente") @Email(message = "E-mail inválido") String clienteEmail,
        @NotBlank(message = "Informe o CPF") String clienteCpf,
        @NotBlank(message = "Informe a conta do Banco Tranquilo") String numeroConta,
        @NotEmpty(message = "Informe ao menos um item") List<@Valid ItemCompraDTO> itens
) {
}
