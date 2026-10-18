package br.com.fiap.mercadoexpress.loja.dtos;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
        long totalPedidos,
        long pedidosAprovados,
        long pedidosRecusados,
        long pedidosAguardando,
        BigDecimal faturamento,
        BigDecimal custo,
        BigDecimal lucro,
        BigDecimal ticketMedio,
        double taxaAprovacao,
        long itensVendidos,
        long emailsEnviados,
        long emailsComFalha,
        BigDecimal valorEstoque,
        long produtosEstoqueBaixo,
        Serie vendasPorDia,
        Serie lucroPorDia,
        Serie statusPedidos,
        Serie topProdutos,
        Serie faturamentoPorCategoria,
        Serie estoquePorProduto
) {
    public record Serie(List<String> labels, List<? extends Number> valores) {
    }
}
