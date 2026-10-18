package br.com.fiap.mercadoexpress.loja.config;

public final class RabbitNames {

    private RabbitNames() {
    }

    public static final String BANCO_EXCHANGE = "banco.tranquilo.pagamentos.exchange";
    public static final String RK_PAGAMENTO_SOLICITADO = "pagamento.solicitado";

    public static final String MERCADO_EXCHANGE = "mercado.express.pagamentos.exchange";

    public static final String RESPONSE_QUEUE = "mercado.express.pagamentos.response";
    public static final String RK_PAGAMENTO_PROCESSADO = "pagamento.processado";

    public static final String EMAIL_QUEUE = "mercado.express.email.confirmacao";
    public static final String RK_EMAIL_CONFIRMACAO = "email.confirmacao";

    public static final String MERCADO_DLX = "mercado.express.dlx";
    public static final String RESPONSE_DLQ = "mercado.express.pagamentos.response.dlq";
    public static final String EMAIL_DLQ = "mercado.express.email.confirmacao.dlq";
}
