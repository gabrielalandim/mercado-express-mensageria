package br.com.fiap.mercadoexpress.loja.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TDS_MQ_TB_Item_Pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "produto_nome", nullable = false, length = 100)
    private String produtoNome;

    @Column(name = "produto_tipo", nullable = false, length = 60)
    private String produtoTipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "custo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoUnitario;

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public BigDecimal getCustoSubtotal() {
        return custoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
