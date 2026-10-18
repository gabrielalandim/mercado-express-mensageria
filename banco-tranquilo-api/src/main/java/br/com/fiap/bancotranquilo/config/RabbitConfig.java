package br.com.fiap.bancotranquilo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static br.com.fiap.bancotranquilo.config.RabbitNames.*;

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
    public DirectExchange bancoDlx() {
        return new DirectExchange(BANCO_DLX, true, false);
    }

    @Bean
    public Queue pagamentoRequestQueue() {
        return QueueBuilder.durable(REQUEST_QUEUE)
                .deadLetterExchange(BANCO_DLX)
                .deadLetterRoutingKey(REQUEST_DLQ)
                .build();
    }

    @Bean
    public Queue pagamentoRequestDlq() {
        return QueueBuilder.durable(REQUEST_DLQ).build();
    }

    @Bean
    public Binding pagamentoRequestBinding() {
        return BindingBuilder.bind(pagamentoRequestQueue()).to(bancoExchange()).with(RK_PAGAMENTO_SOLICITADO);
    }

    @Bean
    public Binding pagamentoRequestDlqBinding() {
        return BindingBuilder.bind(pagamentoRequestDlq()).to(bancoDlx()).with(REQUEST_DLQ);
    }
}
