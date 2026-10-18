package br.com.fiap.bancotranquilo.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoRequestMessage(
        Long pedidoId,
        String numeroConta,
        String cpf,
        String nomeCliente,
        String emailCliente,
        BigDecimal valor,
        String descricao,
        LocalDateTime solicitadoEm
) {
}
