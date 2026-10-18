package br.com.fiap.mercadoexpress.loja.dtos;

import br.com.fiap.mercadoexpress.loja.models.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(
        Long id,
        String clienteNome,
        String clienteEmail,
        String numeroConta,
        BigDecimal valorTotal,
        String status,
        String codigoTransacao,
        String mensagemBanco,
        LocalDateTime criadoEm,
        LocalDateTime processadoEm,
        List<ItemPedidoDTO> itens
) {
    public static PedidoDTO de(Pedido p) {
        return new PedidoDTO(p.getId(), p.getClienteNome(), p.getClienteEmail(), p.getNumeroConta(),
                p.getValorTotal(), p.getStatus().name(), p.getCodigoTransacao(), p.getMensagemBanco(),
                p.getCriadoEm(), p.getProcessadoEm(),
                p.getItens().stream().map(ItemPedidoDTO::de).toList());
    }
}
