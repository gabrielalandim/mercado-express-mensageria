package br.com.fiap.bancotranquilo.messaging;

import br.com.fiap.bancotranquilo.config.RabbitNames;
import br.com.fiap.bancotranquilo.services.PagamentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoRequestListener {

    private final PagamentoService pagamentoService;
    private final PagamentoResponsePublisher publisher;

    @Value("${banco.delay-processamento-ms:1500}")
    private long delayProcessamento;

    @RabbitListener(queues = RabbitNames.REQUEST_QUEUE)
    public void receber(PagamentoRequestMessage requisicao) {
        log.info("[RabbitMQ] << Solicitação recebida | pedido={} conta={} valor={}",
                requisicao.pedidoId(), requisicao.numeroConta(), requisicao.valor());

        if (requisicao.pedidoId() == null) {
            throw new AmqpRejectAndDontRequeueException("Solicitação sem pedidoId");
        }

        simularProcessamento();
        PagamentoResponseMessage resposta = pagamentoService.processar(requisicao);
        publisher.publicar(resposta);
    }

    private void simularProcessamento() {
        if (delayProcessamento <= 0) {
            return;
        }
        try {
            Thread.sleep(delayProcessamento);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
