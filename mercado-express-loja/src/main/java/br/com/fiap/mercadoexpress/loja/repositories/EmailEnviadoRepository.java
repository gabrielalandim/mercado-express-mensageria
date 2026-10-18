package br.com.fiap.mercadoexpress.loja.repositories;

import br.com.fiap.mercadoexpress.loja.models.EmailEnviado;
import br.com.fiap.mercadoexpress.loja.models.StatusEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailEnviadoRepository extends JpaRepository<EmailEnviado, Long> {

    boolean existsByPedidoIdAndStatus(Long pedidoId, StatusEmail status);

    long countByStatus(StatusEmail status);

    List<EmailEnviado> findAllByOrderByEnviadoEmDesc();

    List<EmailEnviado> findTop10ByOrderByEnviadoEmDesc();

    List<EmailEnviado> findByPedidoIdOrderByEnviadoEmDesc(Long pedidoId);
}
