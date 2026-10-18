package br.com.fiap.mercadoexpress.loja.messaging;

import br.com.fiap.mercadoexpress.loja.config.RabbitNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MensageriaProducer {

    private final RabbitTemplate rabbitTemplate;

    public void solicitarPagamento(PagamentoRequestMessage requisicao) {
        rabbitTemplate.convertAndSend(
                RabbitNames.BANCO_EXCHANGE,
                RabbitNames.RK_PAGAMENTO_SOLICITADO,
                requisicao,
                message -> {
                    message.getMessageProperties().setCorrelationId("pedido-" + requisicao.pedidoId());
                    message.getMessageProperties().setHeader("origem", "mercado-express-loja");
                    return message;
                });
        log.info("[RabbitMQ] >> Solicitação de pagamento enviada | pedido={} valor={} conta={}",
                requisicao.pedidoId(), requisicao.valor(), requisicao.numeroConta());
    }

    public void agendarEmailConfirmacao(Long pedidoId) {
        rabbitTemplate.convertAndSend(
                RabbitNames.MERCADO_EXCHANGE,
                RabbitNames.RK_EMAIL_CONFIRMACAO,
                new EmailConfirmacaoMessage(pedidoId),
                message -> {
                    message.getMessageProperties().setCorrelationId("pedido-" + pedidoId);
                    return message;
                });
        log.info("[RabbitMQ] >> E-mail de confirmação agendado | pedido={}", pedidoId);
    }
}
