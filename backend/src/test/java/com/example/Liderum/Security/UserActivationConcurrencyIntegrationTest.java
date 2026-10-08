package com.example.Liderum.Security;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.UserStatus;
import com.example.Liderum.Repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "jwt.secret=test_only_activation_concurrency_secret",
        "liderum.registration.rate-limit.limit=100"
})
@ActiveProfiles("dev")
class UserActivationConcurrencyIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void concurrentActivationAllowsAtMostOneSuccessfulConsumption() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String ownerUsername = "activation-race-owner-" + suffix;
        String ownerEmail = ownerUsername + "@example.test";
        String staffUsername = "activation-race-staff-" + suffix;

        ResponseEntity<JsonNode> registration = http.postForEntity(url("/auth/register-guild"), Map.of(
                "guildName", "Activation race " + suffix,
                "serverName", "Race server",
                "username", ownerUsername,
                "email", ownerEmail,
                "password", "owner-password-123"), JsonNode.class);
        assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        JsonNode login = http.postForObject(url("/auth/login"), Map.of(
                "username", ownerUsername,
                "password", "owner-password-123"), JsonNode.class);
        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(login.get("token").asText());
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JsonNode> created = http.postForEntity(url("/users"), new HttpEntity<>(Map.of(
                "username", staffUsername,
                "email", staffUsername + "@example.test",
                "role", "SOLDADO"), adminHeaders), JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        long userId = created.getBody().get("id").asLong();
        String activationToken = created.getBody().get("activationToken").asText();
        String activationUrl = url("/auth/activate");
        Map<String, String> activationRequest = Map.of("token", activationToken, "password", "staff-password-123");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<ResponseEntity<String>> first = executor.submit(() -> activateTogether(ready, start, activationUrl, activationRequest));
            Future<ResponseEntity<String>> second = executor.submit(() -> activateTogether(ready, start, activationUrl, activationRequest));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            var firstStatus = first.get(30, TimeUnit.SECONDS).getStatusCode();
            var secondStatus = second.get(30, TimeUnit.SECONDS).getStatusCode();
            assertThat(java.util.List.of(firstStatus, secondStatus))
                    .containsExactlyInAnyOrder(HttpStatus.NO_CONTENT, HttpStatus.BAD_REQUEST);
        } finally {
            start.countDown();
        }

        User activated = userRepository.findById(userId).orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(activated.getPassword()).isNotBlank().isNotEqualTo("staff-password-123");
    }

    private ResponseEntity<String> activateTogether(CountDownLatch ready, CountDownLatch start,
                                                     String activationUrl, Map<String, String> request) throws Exception {
        ready.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Concurrent activation start timed out");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.postForEntity(activationUrl, new HttpEntity<>(request, headers), String.class);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
