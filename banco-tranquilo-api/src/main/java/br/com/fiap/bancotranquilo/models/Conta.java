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
@Table(name = "TDS_BT_TB_Conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_conta", nullable = false, unique = true, length = 20)
    private String numeroConta;

    @Column(nullable = false, length = 100)
    private String titular;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(length = 120)
    private String email;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Version
    private Long versao;

    public Conta(String numeroConta, String titular, String cpf, String email, BigDecimal saldo, boolean ativa) {
        this.numeroConta = numeroConta;
        this.titular = titular;
        this.cpf = cpf;
        this.email = email;
        this.saldo = saldo;
        this.ativa = ativa;
    }

    @PrePersist
    void prePersist() {
        if (criadaEm == null) {
            criadaEm = LocalDateTime.now();
        }
    }
}
