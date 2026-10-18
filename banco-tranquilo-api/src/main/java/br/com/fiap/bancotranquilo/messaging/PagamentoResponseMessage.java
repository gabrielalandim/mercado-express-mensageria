package br.com.fiap.bancotranquilo.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseMessage(
        Long pedidoId,
        String codigoTransacao,
        String status,
        String mensagem,
        BigDecimal valor,
        LocalDateTime processadoEm
) {
}
