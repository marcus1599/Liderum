package com.example.liderumnotification.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public Queue guildEventCreatedQueue() {
        return new Queue(guildEventCreatedQueue, true);
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
                "com.example.Liderum.Messaging.GuildEventCreatedMessage", GuildEventCreatedMessage.class
        ));
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
