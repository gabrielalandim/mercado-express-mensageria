package br.com.fiap.mercadoexpress.loja.controllers.api;

import br.com.fiap.mercadoexpress.loja.dtos.CompraRequestDTO;
import br.com.fiap.mercadoexpress.loja.dtos.DashboardDTO;
import br.com.fiap.mercadoexpress.loja.dtos.EmailDTO;
import br.com.fiap.mercadoexpress.loja.dtos.PedidoDTO;
import br.com.fiap.mercadoexpress.loja.models.Produto;
import br.com.fiap.mercadoexpress.loja.repositories.ProdutoRepository;
import br.com.fiap.mercadoexpress.loja.services.DashboardService;
import br.com.fiap.mercadoexpress.loja.services.EmailService;
import br.com.fiap.mercadoexpress.loja.services.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompraApiController {

    private final PedidoService pedidoService;
    private final EmailService emailService;
    private final DashboardService dashboardService;
    private final ProdutoRepository produtoRepository;

    @PostMapping("/compras")
    public ResponseEntity<PedidoDTO> comprar(@Valid @RequestBody CompraRequestDTO compra) {
        PedidoDTO pedido = PedidoDTO.de(pedidoService.criarPedido(compra));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(pedido.id()).toUri();
        return ResponseEntity.accepted().location(location).body(pedido);
    }

    @GetMapping("/compras")
    public List<PedidoDTO> listar() {
        return pedidoService.listarTodos().stream().map(PedidoDTO::de).toList();
    }

    @GetMapping("/compras/{id}")
    public ResponseEntity<PedidoDTO> buscar(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(p -> ResponseEntity.ok(PedidoDTO.de(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/compras/{id}/emails")
    public List<EmailDTO> emailsDaCompra(@PathVariable Long id) {
        return emailService.listarPorPedido(id).stream().map(EmailDTO::de).toList();
    }

    @GetMapping("/produtos")
    public List<Produto> produtos() {
        return produtoRepository.findAllByOrderByNomeAsc();
    }

    @GetMapping("/emails")
    public List<EmailDTO> emails() {
        return emailService.listarTodos().stream().map(EmailDTO::de).toList();
    }

    @GetMapping("/dashboard")
    public DashboardDTO dashboard() {
        return dashboardService.gerar();
    }
}
