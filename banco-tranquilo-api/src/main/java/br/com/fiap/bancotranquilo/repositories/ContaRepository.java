package br.com.fiap.bancotranquilo.repositories;

import br.com.fiap.bancotranquilo.models.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByNumeroConta(String numeroConta);

    boolean existsByNumeroConta(String numeroConta);

    List<Conta> findAllByOrderByNumeroContaAsc();
}
