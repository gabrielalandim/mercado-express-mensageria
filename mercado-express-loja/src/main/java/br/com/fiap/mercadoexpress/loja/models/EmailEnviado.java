package br.com.fiap.mercadoexpress.loja.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TDS_MQ_TB_Email")
public class EmailEnviado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Column(columnDefinition = "TEXT")
    private String corpo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmail status;

    @Column(length = 500)
    private String erro;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;
}
