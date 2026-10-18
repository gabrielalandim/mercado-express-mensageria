package br.com.fiap.bancotranquilo.messaging;

import br.com.fiap.bancotranquilo.config.RabbitNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoResponsePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(PagamentoResponseMessage resposta) {
        rabbitTemplate.convertAndSend(
                RabbitNames.MERCADO_EXCHANGE,
                RabbitNames.RK_PAGAMENTO_PROCESSADO,
                resposta,
                message -> {
                    message.getMessageProperties().setCorrelationId("pedido-" + resposta.pedidoId());
                    message.getMessageProperties().setHeader("origem", "banco-tranquilo-api");
                    return message;
                });
        log.info("[RabbitMQ] >> Resposta enviada ao Mercado Express | pedido={} status={} transacao={}",
                resposta.pedidoId(), resposta.status(), resposta.codigoTransacao());
    }
}
