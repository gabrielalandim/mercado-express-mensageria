package br.com.fiap.mercadoexpress.loja.dtos;

import br.com.fiap.mercadoexpress.loja.models.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoDTO(Long produtoId, String produtoNome, Integer quantidade,
                            BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemPedidoDTO de(ItemPedido i) {
        return new ItemPedidoDTO(i.getProdutoId(), i.getProdutoNome(), i.getQuantidade(),
                i.getPrecoUnitario(), i.getSubtotal());
    }
}
