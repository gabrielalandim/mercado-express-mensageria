package br.com.fiap.bancotranquilo.services;

import br.com.fiap.bancotranquilo.dtos.ContaRequestDTO;
import br.com.fiap.bancotranquilo.exceptions.NegocioException;
import br.com.fiap.bancotranquilo.models.Conta;
import br.com.fiap.bancotranquilo.models.Transacao;
import br.com.fiap.bancotranquilo.repositories.ContaRepository;
import br.com.fiap.bancotranquilo.repositories.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    @Transactional(readOnly = true)
    public List<Conta> listar() {
        return contaRepository.findAllByOrderByNumeroContaAsc();
    }

    @Transactional(readOnly = true)
    public Conta buscar(String numeroConta) {
        return contaRepository.findByNumeroConta(numeroConta)
                .orElseThrow(() -> new NegocioException("Conta " + numeroConta + " não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Transacao> extrato(String numeroConta) {
        buscar(numeroConta);
        return transacaoRepository.findByNumeroContaOrderByProcessadaEmDesc(numeroConta);
    }

    @Transactional
    public Conta criar(ContaRequestDTO dto) {
        String numero = dto.numeroConta().trim();
        if (contaRepository.existsByNumeroConta(numero)) {
            throw new NegocioException("Já existe a conta " + numero + ".");
        }
        String cpf = PagamentoService.somenteDigitos(dto.cpf());
        if (cpf.length() != 11) {
            throw new NegocioException("CPF inválido: informe os 11 dígitos.");
        }
        BigDecimal saldo = dto.saldoInicial() == null ? BigDecimal.ZERO : dto.saldoInicial();
        return contaRepository.save(new Conta(numero, dto.titular().trim(), cpf, dto.email(), saldo, true));
    }

    @Transactional
    public Conta depositar(String numeroConta, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O valor do depósito deve ser maior que zero.");
        }
        Conta conta = buscar(numeroConta);
        conta.setSaldo(conta.getSaldo().add(valor));
        return conta;
    }

    @Transactional
    public Conta alterarStatus(String numeroConta, boolean ativa) {
        Conta conta = buscar(numeroConta);
        conta.setAtiva(ativa);
        return conta;
    }
}
