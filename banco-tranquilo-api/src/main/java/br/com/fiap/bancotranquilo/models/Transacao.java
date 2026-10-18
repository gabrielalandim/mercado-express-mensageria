package br.com.fiap.bancotranquilo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TDS_BT_TB_Transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "numero_conta", length = 20)
    private String numeroConta;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTransacao status;

    @Column(length = 255)
    private String mensagem;

    @Column(length = 255)
    private String descricao;

    @Column(name = "saldo_apos", precision = 14, scale = 2)
    private BigDecimal saldoApos;

    @Column(name = "processada_em", nullable = false)
    private LocalDateTime processadaEm;
}
