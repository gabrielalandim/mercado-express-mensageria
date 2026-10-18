package br.com.fiap.mercadoexpress.loja.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCompraDTO(
        @NotNull(message = "Informe o produtoId") Long produtoId,
        @NotNull(message = "Informe a quantidade") @Min(value = 1, message = "A quantidade mínima é 1") Integer quantidade
) {
}
