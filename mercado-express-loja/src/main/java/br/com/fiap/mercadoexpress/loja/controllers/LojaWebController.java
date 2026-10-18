package br.com.fiap.mercadoexpress.loja.controllers;

import br.com.fiap.mercadoexpress.loja.dtos.CheckoutForm;
import br.com.fiap.mercadoexpress.loja.dtos.CompraRequestDTO;
import br.com.fiap.mercadoexpress.loja.dtos.ItemCompraDTO;
import br.com.fiap.mercadoexpress.loja.dtos.LinhaCarrinho;
import br.com.fiap.mercadoexpress.loja.exceptions.NegocioException;
import br.com.fiap.mercadoexpress.loja.models.Pedido;
import br.com.fiap.mercadoexpress.loja.repositories.ProdutoRepository;
import br.com.fiap.mercadoexpress.loja.services.Carrinho;
import br.com.fiap.mercadoexpress.loja.services.EmailService;
import br.com.fiap.mercadoexpress.loja.services.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LojaWebController {

    private final ProdutoRepository produtoRepository;
    private final PedidoService pedidoService;
    private final EmailService emailService;
    private final Carrinho carrinho;

    @ModelAttribute("qtdCarrinho")
    public int qtdCarrinho() {
        return carrinho.getTotalItens();
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/loja";
    }

    @GetMapping("/loja")
    public String loja(Model model) {
        model.addAttribute("produtos", produtoRepository.findAllByOrderByNomeAsc());
        return "loja/catalogo";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionar(@RequestParam Long produtoId,
                            @RequestParam(defaultValue = "1") int quantidade,
                            RedirectAttributes redirect) {
        produtoRepository.findById(produtoId).ifPresentOrElse(produto -> {
            carrinho.adicionar(produtoId, quantidade);
            redirect.addFlashAttribute("mensagemSucesso", produto.getNome() + " adicionado ao carrinho 🛒");
        }, () -> redirect.addFlashAttribute("mensagemErro", "Produto não encontrado."));
        return "redirect:/loja";
    }

    @GetMapping("/carrinho")
    public String verCarrinho(Model model) {
        if (!model.containsAttribute("checkout")) {
            model.addAttribute("checkout", new CheckoutForm());
        }
        preencherCarrinho(model);
        return "loja/carrinho";
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizar(@RequestParam Long produtoId, @RequestParam int quantidade) {
        carrinho.atualizar(produtoId, quantidade);
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/remover")
    public String remover(@RequestParam Long produtoId) {
        carrinho.remover(produtoId);
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/finalizar")
    public String finalizar(@Valid @ModelAttribute("checkout") CheckoutForm checkout,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (carrinho.isVazio()) {
            redirect.addFlashAttribute("mensagemErro", "Seu carrinho está vazio.");
            return "redirect:/loja";
        }
        if (result.hasErrors()) {
            preencherCarrinho(model);
            return "loja/carrinho";
        }

        List<ItemCompraDTO> itens = carrinho.getItens().entrySet().stream()
                .map(e -> new ItemCompraDTO(e.getKey(), e.getValue()))
                .toList();
        CompraRequestDTO compra = new CompraRequestDTO(checkout.getClienteNome(), checkout.getClienteEmail(),
                checkout.getClienteCpf(), checkout.getNumeroConta(), itens);

        try {
            Pedido pedido = pedidoService.criarPedido(compra);
            carrinho.limpar();
            redirect.addFlashAttribute("mensagemSucesso",
                    "Pedido #" + pedido.getId() + " criado! Aguardando a confirmação do Banco Tranquilo...");
            return "redirect:/pedidos/" + pedido.getId();
        } catch (NegocioException e) {
            model.addAttribute("mensagemErro", e.getMessage());
            preencherCarrinho(model);
            return "loja/carrinho";
        }
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "pedidos/lista";
    }

    @GetMapping("/pedidos/{id}")
    public String pedido(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return pedidoService.buscarPorId(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    model.addAttribute("emails", emailService.listarPorPedido(id));
                    return "pedidos/detalhe";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("mensagemErro", "Pedido #" + id + " não encontrado.");
                    return "redirect:/pedidos";
                });
    }

    @GetMapping("/emails")
    public String emails(Model model) {
        model.addAttribute("emails", emailService.listarTodos());
        return "pedidos/emails";
    }

    private void preencherCarrinho(Model model) {
        List<LinhaCarrinho> linhas = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : carrinho.getItens().entrySet()) {
            produtoRepository.findById(e.getKey())
                    .ifPresent(produto -> linhas.add(new LinhaCarrinho(produto, e.getValue())));
        }
        BigDecimal total = linhas.stream().map(LinhaCarrinho::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("linhas", linhas);
        model.addAttribute("total", total);
    }
}
