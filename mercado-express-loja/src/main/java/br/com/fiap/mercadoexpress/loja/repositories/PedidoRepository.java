package br.com.fiap.mercadoexpress.loja.repositories;

import br.com.fiap.mercadoexpress.loja.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByCriadoEmDesc();

    List<Pedido> findTop10ByOrderByCriadoEmDesc();
}
