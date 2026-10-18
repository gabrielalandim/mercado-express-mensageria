package br.com.fiap.bancotranquilo.repositories;

import br.com.fiap.bancotranquilo.models.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Optional<Transacao> findByPedidoId(Long pedidoId);

    List<Transacao> findAllByOrderByProcessadaEmDesc();

    List<Transacao> findByNumeroContaOrderByProcessadaEmDesc(String numeroConta);
}
