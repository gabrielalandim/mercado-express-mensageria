package br.com.fiap.mercadoexpress.loja.messaging;

import br.com.fiap.mercadoexpress.loja.config.RabbitNames;
import br.com.fiap.mercadoexpress.loja.services.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoResponseListener {

    private final PedidoService pedidoService;

    @RabbitListener(queues = RabbitNames.RESPONSE_QUEUE)
    public void receber(PagamentoResponseMessage resposta) {
        log.info("[RabbitMQ] << Resposta do Banco Tranquilo | pedido={} status={} transacao={} msg={}",
                resposta.pedidoId(), resposta.status(), resposta.codigoTransacao(), resposta.mensagem());
        pedidoService.processarRetornoPagamento(resposta);
    }
}
