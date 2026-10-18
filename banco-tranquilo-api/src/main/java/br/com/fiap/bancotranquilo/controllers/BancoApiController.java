package br.com.fiap.bancotranquilo.controllers;

import br.com.fiap.bancotranquilo.dtos.ContaRequestDTO;
import br.com.fiap.bancotranquilo.dtos.DepositoDTO;
import br.com.fiap.bancotranquilo.messaging.PagamentoRequestMessage;
import br.com.fiap.bancotranquilo.messaging.PagamentoResponseMessage;
import br.com.fiap.bancotranquilo.models.Conta;
import br.com.fiap.bancotranquilo.models.Transacao;
import br.com.fiap.bancotranquilo.services.ContaService;
import br.com.fiap.bancotranquilo.services.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BancoApiController {

    private final ContaService contaService;
    private final PagamentoService pagamentoService;

    @GetMapping("/")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("servico", "Banco Tranquilo - API de Pagamentos");
        info.put("filaRequisicoes", "banco.tranquilo.pagamentos.request");
        info.put("exchangeRespostas", "mercado.express.pagamentos.exchange (pagamento.processado)");
        info.put("endpoints", List.of("/api/contas", "/api/transacoes", "/api/pagamentos"));
        return info;
    }

    @GetMapping("/api/contas")
    public List<Conta> contas() {
        return contaService.listar();
    }

    @GetMapping("/api/contas/{numero}")
    public Conta conta(@PathVariable String numero) {
        return contaService.buscar(numero);
    }

    @GetMapping("/api/contas/{numero}/extrato")
    public List<Transacao> extrato(@PathVariable String numero) {
        return contaService.extrato(numero);
    }

    @PostMapping("/api/contas")
    @ResponseStatus(HttpStatus.CREATED)
    public Conta criarConta(@Valid @RequestBody ContaRequestDTO dto) {
        return contaService.criar(dto);
    }

    @PostMapping("/api/contas/{numero}/deposito")
    public Conta depositar(@PathVariable String numero, @Valid @RequestBody DepositoDTO dto) {
        return contaService.depositar(numero, dto.valor());
    }

    @PatchMapping("/api/contas/{numero}/status")
    public Conta status(@PathVariable String numero, @RequestParam boolean ativa) {
        return contaService.alterarStatus(numero, ativa);
    }

    @GetMapping("/api/transacoes")
    public List<Transacao> transacoes() {
        return pagamentoService.listarTransacoes();
    }

    @PostMapping("/api/pagamentos")
    public PagamentoResponseMessage pagar(@RequestBody PagamentoRequestMessage requisicao) {
        return pagamentoService.processar(requisicao);
    }
}
