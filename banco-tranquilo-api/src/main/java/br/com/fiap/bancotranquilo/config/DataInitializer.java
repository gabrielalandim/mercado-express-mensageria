package br.com.fiap.bancotranquilo.config;

import br.com.fiap.bancotranquilo.models.Conta;
import br.com.fiap.bancotranquilo.repositories.ContaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ContaRepository contaRepository;

    @Value("${banco.seed-demo-data:true}")
    private boolean seed;

    @Override
    public void run(String... args) {
        if (!seed || contaRepository.count() > 0) {
            return;
        }
        contaRepository.saveAll(List.of(
                new Conta("1001-0", "Maria Gabriela Landim Severo", "11111111111", "gabi@email.com", new BigDecimal("5000.00"), true),
                new Conta("1002-0", "Samara Porto Souza", "22222222222", "samara@email.com", new BigDecimal("10000.00"), true),
                new Conta("2001-0", "Cliente Sem Saldo", "33333333333", "semsaldo@email.com", new BigDecimal("20.00"), true),
                new Conta("3001-0", "Cliente Conta Bloqueada", "44444444444", "bloqueada@email.com", new BigDecimal("1000.00"), false)
        ));
        log.info("Contas de teste do Banco Tranquilo criadas.");
    }
}
