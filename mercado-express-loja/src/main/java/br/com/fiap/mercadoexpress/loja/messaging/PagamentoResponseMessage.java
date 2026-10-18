package br.com.fiap.mercadoexpress.loja.messaging;

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
    public boolean aprovado() {
        return "APROVADO".equalsIgnoreCase(status);
    }
}
