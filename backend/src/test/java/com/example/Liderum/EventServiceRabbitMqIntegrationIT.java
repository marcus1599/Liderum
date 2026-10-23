package com.example.Liderum;

import com.example.Liderum.Entities.Event;
import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Messaging.GuildEventCreatedMessage;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.EventService;
import com.example.Liderum.dto.EventRequestDTO;
import com.example.Liderum.dto.EventResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=test_only_rabbit_publisher_secret_32_bytes_minimum",
        "spring.datasource.url=jdbc:h2:mem:event_rabbit_integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.rabbitmq.addresses=${RABBITMQ_TEST_ADDRESSES:amqp://guest:guest@localhost:5673}",
        "liderum.rabbitmq.guild-event-created-queue=notification.guild-event-created.backend-it"
})
@ActiveProfiles("default")
@EnabledIfSystemProperty(named = "rabbitmq.integration", matches = "true")
@EnabledIfEnvironmentVariable(named = "RABBITMQ_TEST_ADDRESSES", matches = "^amqp://.*")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class EventServiceRabbitMqIntegrationIT {

    private static final String TEST_QUEUE = "notification.guild-event-created.backend-it";

    @Autowired private EventService eventService;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private GuildRepository guildRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void persistsTenantScopedEventAndPublishesExpectedMessageToConfiguredRoute() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Guild guild = guildRepository.save(Guild.builder().name("Rabbit integration " + suffix).build());
        String username = "rabbit-it-" + suffix;
        userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.test")
                .password("test-only-not-used")
                .guildRole(GuildRole.MARECHAL)
                .guild(guild)
                .build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "not-a-password", List.of(new SimpleGrantedAuthority("ROLE_MARECHAL"))));

        String eventName = "GuildEventCreated " + suffix;
        LocalDateTime eventDate = LocalDateTime.of(2030, 5, 6, 19, 30);
        EventResponseDTO created = eventService.create(EventRequestDTO.builder()
                .name(eventName)
                .date(eventDate)
                .description("synthetic integration fixture")
                .build());

        Event persisted = eventRepository.findById(created.getId()).orElseThrow();
        assertThat(persisted.getGuild().getId()).isEqualTo(guild.getId());

        Object published = rabbitTemplate.receiveAndConvert(TEST_QUEUE, 10_000);
        assertThat(published).isInstanceOf(GuildEventCreatedMessage.class);
        GuildEventCreatedMessage message = (GuildEventCreatedMessage) published;
        assertThat(message.eventId()).isEqualTo(created.getId());
        assertThat(message.eventName()).isEqualTo(eventName);
        assertThat(message.date()).isEqualTo(eventDate);
        assertThat(rabbitTemplate.receive(TEST_QUEUE, 250L)).isNull();
    }
}
