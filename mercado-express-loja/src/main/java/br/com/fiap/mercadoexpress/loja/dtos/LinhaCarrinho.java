package br.com.fiap.mercadoexpress.loja.dtos;

import br.com.fiap.mercadoexpress.loja.models.Produto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class LinhaCarrinho {
    private final Produto produto;
    private final int quantidade;

    public BigDecimal getSubtotal() {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }
}
