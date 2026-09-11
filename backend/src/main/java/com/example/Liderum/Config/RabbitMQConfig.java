package com.example.Liderum.Config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${liderum.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${liderum.rabbitmq.guild-event-created-routing-key}")
    private String guildEventCreatedRoutingKey;

    @Value("${liderum.rabbitmq.guild-event-created-queue}")
    private String guildEventCreatedQueue;

    private static final String DEAD_LETTER_EXCHANGE = "liderum.events.dlx";
    private static final String DEAD_LETTER_ROUTING_KEY = "guild.event.created.dlq";

    @Bean
    public DirectExchange liderumEventsExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

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

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
