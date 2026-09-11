package com.example.liderumnotification.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${liderum.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${liderum.rabbitmq.guild-event-created-routing-key}")
    private String guildEventCreatedRoutingKey;

    @Value("${liderum.rabbitmq.guild-event-created-queue}")
    private String guildEventCreatedQueue;

    @Bean
    public DirectExchange liderumEventsExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    private static final String DEAD_LETTER_EXCHANGE = "liderum.events.dlx";
    private static final String DEAD_LETTER_ROUTING_KEY = "guild.event.created.dlq";

    @Bean
    public Queue guildEventCreatedQueue() {
        return QueueBuilder.durable(guildEventCreatedQueue)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange liderumEventsDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue guildEventCreatedDeadLetterQueue() {
        return QueueBuilder.durable(guildEventCreatedQueue + ".dlq").build();
    }

    @Bean
    public Binding guildEventCreatedDeadLetterBinding() {
        return BindingBuilder.bind(guildEventCreatedDeadLetterQueue())
                .to(liderumEventsDeadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }

    @Bean
    public Binding guildEventCreatedBinding() {
        return BindingBuilder
                .bind(guildEventCreatedQueue())
                .to(liderumEventsExchange())
                .with(guildEventCreatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JsonMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setIdClassMapping(Map.of(
                "com.example.Liderum.Messaging.GuildEventCreatedMessage", GuildEventCreatedMessage.class,
                "com.example.liderumnotification.messaging.GuildEventCreatedMessage", GuildEventCreatedMessage.class
        ));
        typeMapper.setTrustedPackages("com.example.Liderum.Messaging", "com.example.liderumnotification.messaging");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
