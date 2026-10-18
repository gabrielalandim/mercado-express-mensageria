package br.com.fiap.mercadoexpress.loja.messaging;

import br.com.fiap.mercadoexpress.loja.services.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventosTransacionais {

    private final MensageriaProducer producer;
    private final PedidoService pedidoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarPedido(PedidoCriadoEvent evento) {
        try {
            producer.solicitarPagamento(evento.requisicao());
        } catch (AmqpException e) {
            log.error("Falha ao publicar a solicitação do pedido {}: {}", evento.requisicao().pedidoId(), e.getMessage());
            pedidoService.marcarErroComunicacao(evento.requisicao().pedidoId(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAprovarPagamento(PagamentoAprovadoEvent evento) {
        try {
            producer.agendarEmailConfirmacao(evento.pedidoId());
        } catch (AmqpException e) {
            log.error("Falha ao agendar e-mail do pedido {}: {}", evento.pedidoId(), e.getMessage());
        }
    }
}
