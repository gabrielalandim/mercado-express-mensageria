package br.com.fiap.bancotranquilo.config;

public final class RabbitNames {

    private RabbitNames() {
    }

    public static final String BANCO_EXCHANGE = "banco.tranquilo.pagamentos.exchange";
    public static final String REQUEST_QUEUE = "banco.tranquilo.pagamentos.request";
    public static final String RK_PAGAMENTO_SOLICITADO = "pagamento.solicitado";

    public static final String MERCADO_EXCHANGE = "mercado.express.pagamentos.exchange";
    public static final String RK_PAGAMENTO_PROCESSADO = "pagamento.processado";

    public static final String BANCO_DLX = "banco.tranquilo.dlx";
    public static final String REQUEST_DLQ = "banco.tranquilo.pagamentos.request.dlq";
}
