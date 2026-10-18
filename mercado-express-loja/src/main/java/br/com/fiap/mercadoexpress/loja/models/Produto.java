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
@Table(name = "TDS_Sec_MVC_TB_Mercado")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, length = 60)
    private String setor;

    @Column(nullable = false, length = 40)
    private String tamanho;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "preco_custo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Column(nullable = false)
    private Integer estoque;

    public Produto(String nome, String tipo, String setor, String tamanho,
                   BigDecimal preco, BigDecimal precoCusto, Integer estoque) {
        this.nome = nome;
        this.tipo = tipo;
        this.setor = setor;
        this.tamanho = tamanho;
        this.preco = preco;
        this.precoCusto = precoCusto;
        this.estoque = estoque;
    }
}
