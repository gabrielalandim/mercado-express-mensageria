package br.com.fiap.bancotranquilo.services;

import br.com.fiap.bancotranquilo.messaging.PagamentoRequestMessage;
import br.com.fiap.bancotranquilo.messaging.PagamentoResponseMessage;
import br.com.fiap.bancotranquilo.models.Conta;
import br.com.fiap.bancotranquilo.models.StatusTransacao;
import br.com.fiap.bancotranquilo.models.Transacao;
import br.com.fiap.bancotranquilo.repositories.ContaRepository;
import br.com.fiap.bancotranquilo.repositories.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    @Transactional
    public PagamentoResponseMessage processar(PagamentoRequestMessage req) {
        if (req.pedidoId() == null) {
            throw new IllegalArgumentException("pedidoId é obrigatório");
        }

        Optional<Transacao> existente = transacaoRepository.findByPedidoId(req.pedidoId());
        if (existente.isPresent()) {
            log.info("Pedido #{} já processado (transação {}); reenviando o mesmo resultado.",
                    req.pedidoId(), existente.get().getCodigo());
            return paraResposta(existente.get());
        }

        BigDecimal valor = req.valor();
        String numeroConta = req.numeroConta() == null ? "" : req.numeroConta().trim();

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return registrar(req, numeroConta, StatusTransacao.RECUSADO, "Valor inválido para pagamento.", null);
        }

        Optional<Conta> encontrada = contaRepository.findByNumeroConta(numeroConta);
        if (encontrada.isEmpty()) {
            return registrar(req, numeroConta, StatusTransacao.RECUSADO,
                    "Conta " + numeroConta + " não encontrada no Banco Tranquilo.", null);
        }

        Conta conta = encontrada.get();
        if (!conta.isAtiva()) {
            return registrar(req, numeroConta, StatusTransacao.RECUSADO, "Conta bloqueada. Procure sua agência.", conta.getSaldo());
        }
        if (!conta.getCpf().equals(somenteDigitos(req.cpf()))) {
            return registrar(req, numeroConta, StatusTransacao.RECUSADO, "CPF não confere com o titular da conta.", conta.getSaldo());
        }
        if (conta.getSaldo().compareTo(valor) < 0) {
            return registrar(req, numeroConta, StatusTransacao.RECUSADO, "Saldo insuficiente.", conta.getSaldo());
        }

        conta.setSaldo(conta.getSaldo().subtract(valor));
        return registrar(req, numeroConta, StatusTransacao.APROVADO,
                "Pagamento aprovado pelo Banco Tranquilo.", conta.getSaldo());
    }

    @Transactional(readOnly = true)
    public List<Transacao> listarTransacoes() {
        return transacaoRepository.findAllByOrderByProcessadaEmDesc();
    }

    private PagamentoResponseMessage registrar(PagamentoRequestMessage req, String numeroConta,
                                               StatusTransacao status, String mensagem, BigDecimal saldoApos) {
        Transacao t = new Transacao();
        t.setCodigo("BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        t.setPedidoId(req.pedidoId());
        t.setNumeroConta(numeroConta);
        t.setValor(req.valor() == null ? BigDecimal.ZERO : req.valor());
        t.setStatus(status);
        t.setMensagem(mensagem);
        t.setDescricao(req.descricao());
        t.setSaldoApos(saldoApos);
        t.setProcessadaEm(LocalDateTime.now());
        transacaoRepository.save(t);

        log.info("Pedido #{} | conta {} | R$ {} -> {} ({})", req.pedidoId(), numeroConta, t.getValor(), status, mensagem);
        return paraResposta(t);
    }

    private static PagamentoResponseMessage paraResposta(Transacao t) {
        return new PagamentoResponseMessage(t.getPedidoId(), t.getCodigo(), t.getStatus().name(),
                t.getMensagem(), t.getValor(), t.getProcessadaEm());
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
