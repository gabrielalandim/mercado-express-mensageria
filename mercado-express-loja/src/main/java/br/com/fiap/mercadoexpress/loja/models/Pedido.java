package br.com.fiap.mercadoexpress.loja.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TDS_MQ_TB_Pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nome", nullable = false, length = 100)
    private String clienteNome;

    @Column(name = "cliente_email", nullable = false, length = 120)
    private String clienteEmail;

    @Column(name = "cliente_cpf", nullable = false, length = 11)
    private String clienteCpf;

    @Column(name = "numero_conta", nullable = false, length = 20)
    private String numeroConta;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "custo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "codigo_transacao", length = 60)
    private String codigoTransacao;

    @Column(name = "mensagem_banco", length = 255)
    private String mensagemBanco;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedido> itens = new ArrayList<>();

    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        itens.add(item);
    }

    public BigDecimal getLucro() {
        return valorTotal.subtract(custoTotal);
    }

    public int getQuantidadeItens() {
        return itens.stream().mapToInt(ItemPedido::getQuantidade).sum();
    }

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
