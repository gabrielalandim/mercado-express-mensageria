package br.com.fiap.mercadoexpress.loja.messaging;

import br.com.fiap.mercadoexpress.loja.config.RabbitNames;
import br.com.fiap.mercadoexpress.loja.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitNames.EMAIL_QUEUE)
    public void receber(EmailConfirmacaoMessage mensagem) {
        log.info("[RabbitMQ] << Envio de e-mail solicitado | pedido={}", mensagem.pedidoId());
        emailService.enviarConfirmacaoPagamento(mensagem.pedidoId());
    }
}
