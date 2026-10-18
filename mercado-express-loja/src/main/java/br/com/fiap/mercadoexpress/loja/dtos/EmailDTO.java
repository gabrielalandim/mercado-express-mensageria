package br.com.fiap.mercadoexpress.loja.dtos;

import br.com.fiap.mercadoexpress.loja.models.EmailEnviado;

import java.time.LocalDateTime;

public record EmailDTO(Long id, Long pedidoId, String destinatario, String assunto,
                       String status, String erro, LocalDateTime enviadoEm) {

    public static EmailDTO de(EmailEnviado e) {
        return new EmailDTO(e.getId(), e.getPedidoId(), e.getDestinatario(), e.getAssunto(),
                e.getStatus().name(), e.getErro(), e.getEnviadoEm());
    }
}
