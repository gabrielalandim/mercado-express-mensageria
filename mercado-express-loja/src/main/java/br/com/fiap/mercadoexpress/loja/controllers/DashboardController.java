package br.com.fiap.mercadoexpress.loja.controllers;

import br.com.fiap.mercadoexpress.loja.services.Carrinho;
import br.com.fiap.mercadoexpress.loja.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final Carrinho carrinho;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("qtdCarrinho", carrinho.getTotalItens());
        model.addAttribute("d", dashboardService.gerar());
        model.addAttribute("ultimosPedidos", dashboardService.ultimosPedidos());
        model.addAttribute("ultimosEmails", dashboardService.ultimosEmails());
        model.addAttribute("estoqueBaixo", dashboardService.produtosEstoqueBaixo());
        model.addAttribute("limiteEstoque", DashboardService.LIMITE_ESTOQUE_BAIXO);
        return "loja/dashboard";
    }
}
