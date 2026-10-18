package br.com.fiap.mercadoexpress.loja.services;

import br.com.fiap.mercadoexpress.loja.dtos.CompraRequestDTO;
import br.com.fiap.mercadoexpress.loja.dtos.ItemCompraDTO;
import br.com.fiap.mercadoexpress.loja.exceptions.NegocioException;
import br.com.fiap.mercadoexpress.loja.messaging.PagamentoAprovadoEvent;
import br.com.fiap.mercadoexpress.loja.messaging.PagamentoRequestMessage;
import br.com.fiap.mercadoexpress.loja.messaging.PagamentoResponseMessage;
import br.com.fiap.mercadoexpress.loja.messaging.PedidoCriadoEvent;
import br.com.fiap.mercadoexpress.loja.models.ItemPedido;
import br.com.fiap.mercadoexpress.loja.models.Pedido;
import br.com.fiap.mercadoexpress.loja.models.Produto;
import br.com.fiap.mercadoexpress.loja.models.StatusPedido;
import br.com.fiap.mercadoexpress.loja.repositories.PedidoRepository;
import br.com.fiap.mercadoexpress.loja.repositories.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Pedido criarPedido(CompraRequestDTO compra) {
        String cpf = somenteDigitos(compra.clienteCpf());
        if (cpf.length() != 11) {
            throw new NegocioException("CPF inválido: informe os 11 dígitos.");
        }

        Map<Long, Integer> quantidades = new LinkedHashMap<>();
        for (ItemCompraDTO item : compra.itens()) {
            quantidades.merge(item.produtoId(), item.quantidade(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setClienteNome(compra.clienteNome().trim());
        pedido.setClienteEmail(compra.clienteEmail().trim().toLowerCase());
        pedido.setClienteCpf(cpf);
        pedido.setNumeroConta(compra.numeroConta().trim());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantidades.entrySet()) {
            Produto produto = produtoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new NegocioException("Produto #" + entry.getKey() + " não encontrado."));
            int qtd = entry.getValue();
            if (produto.getEstoque() < qtd) {
                throw new NegocioException("Estoque insuficiente para \"" + produto.getNome()
                        + "\" (disponível: " + produto.getEstoque() + ").");
            }

            ItemPedido item = new ItemPedido();
            item.setProdutoId(produto.getId());
            item.setProdutoNome(produto.getNome());
            item.setProdutoTipo(produto.getTipo());
            item.setQuantidade(qtd);
            item.setPrecoUnitario(produto.getPreco());
            item.setCustoUnitario(produto.getPrecoCusto());
            pedido.adicionarItem(item);

            total = total.add(item.getSubtotal());
            custo = custo.add(item.getCustoSubtotal());
        }

        pedido.setValorTotal(total);
        pedido.setCustoTotal(custo);
        Pedido salvo = pedidoRepository.save(pedido);

        PagamentoRequestMessage requisicao = new PagamentoRequestMessage(
                salvo.getId(),
                salvo.getNumeroConta(),
                salvo.getClienteCpf(),
                salvo.getClienteNome(),
                salvo.getClienteEmail(),
                salvo.getValorTotal(),
                "Compra Mercado Express - Pedido #" + salvo.getId(),
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(new PedidoCriadoEvent(requisicao));

        log.info("Pedido #{} criado | cliente={} total={}", salvo.getId(), salvo.getClienteEmail(), total);
        return salvo;
    }

    @Transactional
    public void processarRetornoPagamento(PagamentoResponseMessage resposta) {
        if (resposta.pedidoId() == null) {
            log.warn("Resposta sem pedidoId ignorada: {}", resposta);
            return;
        }
        Optional<Pedido> encontrado = pedidoRepository.findById(resposta.pedidoId());
        if (encontrado.isEmpty()) {
            log.warn("Pedido #{} não encontrado para a resposta do banco.", resposta.pedidoId());
            return;
        }
        Pedido pedido = encontrado.get();

        if (pedido.getStatus() == StatusPedido.APROVADO || pedido.getStatus() == StatusPedido.RECUSADO) {
            log.info("Pedido #{} já estava {}; resposta duplicada ignorada.", pedido.getId(), pedido.getStatus());
            return;
        }

        pedido.setCodigoTransacao(resposta.codigoTransacao());
        pedido.setMensagemBanco(resposta.mensagem());
        pedido.setProcessadoEm(resposta.processadoEm() != null ? resposta.processadoEm() : LocalDateTime.now());

        if (resposta.aprovado()) {
            pedido.setStatus(StatusPedido.APROVADO);
            baixarEstoque(pedido);
            eventPublisher.publishEvent(new PagamentoAprovadoEvent(pedido.getId()));
            log.info("Pedido #{} APROVADO pelo Banco Tranquilo (transação {}).", pedido.getId(), resposta.codigoTransacao());
        } else {
            pedido.setStatus(StatusPedido.RECUSADO);
            log.info("Pedido #{} RECUSADO pelo Banco Tranquilo: {}", pedido.getId(), resposta.mensagem());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarErroComunicacao(Long pedidoId, String erro) {
        pedidoRepository.findById(pedidoId).ifPresent(p -> {
            p.setStatus(StatusPedido.ERRO_COMUNICACAO);
            p.setMensagemBanco("Falha ao comunicar com o broker: " + abreviar(erro, 200));
        });
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByCriadoEmDesc();
    }

    private void baixarEstoque(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            produtoRepository.findById(item.getProdutoId()).ifPresent(produto -> {
                int novoEstoque = produto.getEstoque() - item.getQuantidade();
                if (novoEstoque < 0) {
                    log.warn("Estoque de \"{}\" ficaria negativo; ajustado para 0.", produto.getNome());
                    novoEstoque = 0;
                }
                produto.setEstoque(novoEstoque);
            });
        }
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private static String abreviar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
