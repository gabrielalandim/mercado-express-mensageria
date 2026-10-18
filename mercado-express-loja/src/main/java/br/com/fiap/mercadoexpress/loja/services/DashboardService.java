package br.com.fiap.mercadoexpress.loja.services;

import br.com.fiap.mercadoexpress.loja.dtos.DashboardDTO;
import br.com.fiap.mercadoexpress.loja.dtos.DashboardDTO.Serie;
import br.com.fiap.mercadoexpress.loja.models.*;
import br.com.fiap.mercadoexpress.loja.repositories.EmailEnviadoRepository;
import br.com.fiap.mercadoexpress.loja.repositories.PedidoRepository;
import br.com.fiap.mercadoexpress.loja.repositories.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    public static final int LIMITE_ESTOQUE_BAIXO = 10;
    private static final int DIAS_GRAFICO = 7;
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final EmailEnviadoRepository emailRepository;

    @Transactional(readOnly = true)
    public DashboardDTO gerar() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        List<Pedido> aprovados = pedidos.stream().filter(p -> p.getStatus() == StatusPedido.APROVADO).toList();
        List<Produto> produtos = produtoRepository.findAllByOrderByNomeAsc();

        long totalPedidos = pedidos.size();
        long qtdAprovados = aprovados.size();
        long qtdRecusados = contar(pedidos, StatusPedido.RECUSADO);
        long qtdAguardando = contar(pedidos, StatusPedido.AGUARDANDO_PAGAMENTO);
        long qtdErro = contar(pedidos, StatusPedido.ERRO_COMUNICACAO);

        BigDecimal faturamento = somar(aprovados, Pedido::getValorTotal);
        BigDecimal custo = somar(aprovados, Pedido::getCustoTotal);
        BigDecimal lucro = faturamento.subtract(custo);
        BigDecimal ticketMedio = qtdAprovados == 0 ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(qtdAprovados), 2, RoundingMode.HALF_UP);
        long finalizados = qtdAprovados + qtdRecusados;
        double taxaAprovacao = finalizados == 0 ? 0 : Math.round(qtdAprovados * 1000.0 / finalizados) / 10.0;
        long itensVendidos = aprovados.stream().mapToLong(Pedido::getQuantidadeItens).sum();

        BigDecimal valorEstoque = produtos.stream()
                .map(p -> p.getPreco().multiply(BigDecimal.valueOf(p.getEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long estoqueBaixo = produtos.stream().filter(p -> p.getEstoque() <= LIMITE_ESTOQUE_BAIXO).count();

        // vendas e lucro por dia (últimos 7 dias)
        LocalDate hoje = LocalDate.now();
        Map<LocalDate, BigDecimal> vendasDia = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> lucroDia = new LinkedHashMap<>();
        for (int i = DIAS_GRAFICO - 1; i >= 0; i--) {
            vendasDia.put(hoje.minusDays(i), BigDecimal.ZERO);
            lucroDia.put(hoje.minusDays(i), BigDecimal.ZERO);
        }
        for (Pedido p : aprovados) {
            LocalDate dia = (p.getProcessadoEm() != null ? p.getProcessadoEm() : p.getCriadoEm()).toLocalDate();
            if (vendasDia.containsKey(dia)) {
                vendasDia.merge(dia, p.getValorTotal(), BigDecimal::add);
                lucroDia.merge(dia, p.getLucro(), BigDecimal::add);
            }
        }

        // status dos pedidos
        Serie status = new Serie(
                List.of("Aprovados", "Recusados", "Aguardando", "Erro de comunicação"),
                List.of(qtdAprovados, qtdRecusados, qtdAguardando, qtdErro));

        // top 5 produtos mais vendidos (quantidade)
        Map<String, Integer> qtdPorProduto = new HashMap<>();
        Map<String, BigDecimal> fatPorCategoria = new TreeMap<>();
        for (Pedido p : aprovados) {
            for (ItemPedido item : p.getItens()) {
                qtdPorProduto.merge(item.getProdutoNome(), item.getQuantidade(), Integer::sum);
                fatPorCategoria.merge(item.getProdutoTipo(), item.getSubtotal(), BigDecimal::add);
            }
        }
        List<Map.Entry<String, Integer>> top = qtdPorProduto.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();

        return new DashboardDTO(
                totalPedidos, qtdAprovados, qtdRecusados, qtdAguardando,
                faturamento, custo, lucro, ticketMedio, taxaAprovacao, itensVendidos,
                emailRepository.countByStatus(StatusEmail.ENVIADO),
                emailRepository.countByStatus(StatusEmail.FALHA),
                valorEstoque, estoqueBaixo,
                new Serie(rotulos(vendasDia.keySet()), new ArrayList<>(vendasDia.values())),
                new Serie(rotulos(lucroDia.keySet()), new ArrayList<>(lucroDia.values())),
                status,
                new Serie(top.stream().map(Map.Entry::getKey).toList(), top.stream().map(Map.Entry::getValue).toList()),
                new Serie(new ArrayList<>(fatPorCategoria.keySet()), new ArrayList<>(fatPorCategoria.values())),
                new Serie(produtos.stream().map(Produto::getNome).toList(), produtos.stream().map(Produto::getEstoque).toList())
        );
    }

    @Transactional(readOnly = true)
    public List<Pedido> ultimosPedidos() {
        return pedidoRepository.findTop10ByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<EmailEnviado> ultimosEmails() {
        return emailRepository.findTop10ByOrderByEnviadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<Produto> produtosEstoqueBaixo() {
        return produtoRepository.findAllByOrderByNomeAsc().stream()
                .filter(p -> p.getEstoque() <= LIMITE_ESTOQUE_BAIXO)
                .collect(Collectors.toList());
    }

    private static long contar(List<Pedido> pedidos, StatusPedido status) {
        return pedidos.stream().filter(p -> p.getStatus() == status).count();
    }

    private static BigDecimal somar(List<Pedido> pedidos, java.util.function.Function<Pedido, BigDecimal> campo) {
        return pedidos.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<String> rotulos(Collection<LocalDate> dias) {
        return dias.stream().map(DIA::format).toList();
    }
}
