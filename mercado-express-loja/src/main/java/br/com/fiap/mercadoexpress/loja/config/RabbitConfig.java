package br.com.fiap.mercadoexpress.loja.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static br.com.fiap.mercadoexpress.loja.config.RabbitNames.*;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange bancoExchange() {
        return new TopicExchange(BANCO_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange mercadoExchange() {
        return new TopicExchange(MERCADO_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange mercadoDlx() {
        return new DirectExchange(MERCADO_DLX, true, false);
    }

    @Bean
    public Queue pagamentoResponseQueue() {
        return QueueBuilder.durable(RESPONSE_QUEUE)
                .deadLetterExchange(MERCADO_DLX)
                .deadLetterRoutingKey(RESPONSE_DLQ)
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .deadLetterExchange(MERCADO_DLX)
                .deadLetterRoutingKey(EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue pagamentoResponseDlq() {
        return QueueBuilder.durable(RESPONSE_DLQ).build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public Binding pagamentoResponseBinding() {
        return BindingBuilder.bind(pagamentoResponseQueue()).to(mercadoExchange()).with(RK_PAGAMENTO_PROCESSADO);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(mercadoExchange()).with(RK_EMAIL_CONFIRMACAO);
    }

    @Bean
    public Binding pagamentoResponseDlqBinding() {
        return BindingBuilder.bind(pagamentoResponseDlq()).to(mercadoDlx()).with(RESPONSE_DLQ);
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq()).to(mercadoDlx()).with(EMAIL_DLQ);
    }
}
