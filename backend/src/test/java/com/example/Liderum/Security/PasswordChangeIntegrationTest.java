package com.example.Liderum.Security;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Enums.UserStatus;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.dto.AuthRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=password_change_test_secret_32_bytes_minimum",
        "liderum.registration.rate-limit.limit=100"
})
@AutoConfigureMockMvc
@ActiveProfiles("default")
class PasswordChangeIntegrationTest {

    private static final String OLD_PASSWORD = "old-password-123";
    private static final String NEW_PASSWORD = "new-password-456";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired GuildRepository guildRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> guildIds = new ArrayList<>();

    @AfterEach
    void cleanFixtures() {
        userRepository.deleteAllById(userIds);
        guildRepository.deleteAllById(guildIds);
    }

    @Test
    void changesOnlyOwnPasswordAndRevokesEveryPreviousToken() throws Exception {
        User owner = createUser("password-owner", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        User other = createUser("password-other", GuildRole.SOLDADO, UserStatus.ACTIVE, OLD_PASSWORD);
        String oldToken = login(owner.getUsername(), OLD_PASSWORD);
        String secondOldToken = login(owner.getUsername(), OLD_PASSWORD);
        String otherToken = login(other.getUsername(), OLD_PASSWORD);

        mockMvc.perform(put("/users/me/password").header("Authorization", bearer(oldToken))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordBody(OLD_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/me").header("Authorization", bearer(oldToken))).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(secondOldToken))).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(otherToken))).andExpect(status().isOk());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(owner.getUsername(), OLD_PASSWORD)))
                .andExpect(status().isUnauthorized());

        String newToken = login(owner.getUsername(), NEW_PASSWORD);
        mockMvc.perform(get("/users/me").header("Authorization", bearer(newToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.credentialVersion").doesNotExist());
        User changed = userRepository.findById(owner.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(NEW_PASSWORD, changed.getPassword())).isTrue();
        assertThat(changed.getCredentialVersion()).isEqualTo(1L);
        assertThat(userRepository.findById(other.getId()).orElseThrow().getCredentialVersion()).isZero();
    }

    @Test
    void everyActiveRoleCanChangeItsOwnPassword() throws Exception {
        for (GuildRole role : GuildRole.values()) {
            User user = createUser("password-role-" + role.name(), role, UserStatus.ACTIVE, OLD_PASSWORD);
            mockMvc.perform(put("/users/me/password").header("Authorization", bearer(login(user.getUsername(), OLD_PASSWORD)))
                            .contentType(MediaType.APPLICATION_JSON).content(passwordBody(OLD_PASSWORD, NEW_PASSWORD)))
                    .andExpect(status().isNoContent());
            assertThat(passwordEncoder.matches(NEW_PASSWORD,
                    userRepository.findById(user.getId()).orElseThrow().getPassword())).isTrue();
        }
    }

    @Test
    void rejectsIncorrectUnchangedAndOversizedUtf8PasswordsWithoutMutation() throws Exception {
        User user = createUser("password-invalid", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        String token = login(user.getUsername(), OLD_PASSWORD);
        String originalHash = user.getPassword();

        passwordChange(token, "incorrect-current", NEW_PASSWORD).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CURRENT_PASSWORD_INVALID"));
        passwordChange(token, OLD_PASSWORD, OLD_PASSWORD).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("PASSWORD_UNCHANGED"));
        passwordChange(token, OLD_PASSWORD, "é".repeat(37)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("INVALID_PASSWORD_REQUEST"));

        User unchanged = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unchanged.getPassword()).isEqualTo(originalHash);
        assertThat(unchanged.getCredentialVersion()).isZero();
        mockMvc.perform(get("/users/me").header("Authorization", bearer(token))).andExpect(status().isOk());
    }

    @Test
    void acceptsExactly72Utf8BytesAndDoesNotReturnSecrets() throws Exception {
        User user = createUser("password-utf8", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        String token = login(user.getUsername(), OLD_PASSWORD);
        String seventyTwoBytes = "é".repeat(36);

        MvcResult result = passwordChange(token, OLD_PASSWORD, seventyTwoBytes)
                .andExpect(status().isNoContent()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(passwordEncoder.matches(seventyTwoBytes,
                userRepository.findById(user.getId()).orElseThrow().getPassword())).isTrue();
    }

    @Test
    void rejectsMissingLegacyAndDisabledAuthenticationAndSurvivesUsernameRecreation() throws Exception {
        User active = createUser("password-legacy", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        String token = login(active.getUsername(), OLD_PASSWORD);
        String legacy = jwtUtilTestToken(active, false);
        String mismatchedVersion = jwtUtilTestToken(active, true);
        mockMvc.perform(get("/users/me").header("Authorization", bearer(legacy))).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(mismatchedVersion))).andExpect(status().isUnauthorized());

        User disabled = createUser("password-disabled", GuildRole.MARECHAL, UserStatus.DISABLED, OLD_PASSWORD);
        String disabledToken = jwtUtil.generateToken(new CustomUserDetails(disabled), List.of(disabled.getGuildRole().name()));
        mockMvc.perform(get("/users/me").header("Authorization", bearer(disabledToken))).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/me")).andExpect(status().isUnauthorized());

        userRepository.deleteById(active.getId());
        userRepository.flush();
        User recreated = createUserWithUsername(active.getUsername(), GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        assertThat(recreated.getId()).isNotEqualTo(active.getId());
        mockMvc.perform(get("/users/me").header("Authorization", bearer(token))).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAnyClientChosenIdentityOrAuthorityAndDoesNotChangeAnotherGuild() throws Exception {
        User owner = createUser("password-no-target", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        User otherGuildUser = createUser("password-other-guild", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        String token = login(owner.getUsername(), OLD_PASSWORD);
        String otherHash = otherGuildUser.getPassword();
        String forged = objectMapper.writeValueAsString(java.util.Map.of(
                "currentPassword", OLD_PASSWORD,
                "newPassword", NEW_PASSWORD,
                "userId", otherGuildUser.getId(),
                "guildId", otherGuildUser.getGuild().getId(),
                "role", "MARECHAL",
                "credentialVersion", 0));

        mockMvc.perform(put("/users/me/password").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(forged))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findById(owner.getId()).orElseThrow().getCredentialVersion()).isZero();
        assertThat(userRepository.findById(otherGuildUser.getId()).orElseThrow().getPassword()).isEqualTo(otherHash);
    }

    @Test
    void serializesConcurrentChangesSoOnlyOneOldCredentialCanSucceed() throws Exception {
        User user = createUser("password-race", GuildRole.MARECHAL, UserStatus.ACTIVE, OLD_PASSWORD);
        String token = login(user.getUsername(), OLD_PASSWORD);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> concurrentChange(token, ready, start));
            Future<Integer> second = executor.submit(() -> concurrentChange(token, ready, start));
            ready.await();
            start.countDown();
            List<Integer> statuses = List.of(first.get(), second.get());
            assertThat(statuses).containsExactlyInAnyOrder(204, 401);
            assertThat(userRepository.findById(user.getId()).orElseThrow().getCredentialVersion()).isEqualTo(1L);
        } finally {
            executor.shutdownNow();
        }
    }

    private int concurrentChange(String token, CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        return passwordChange(token, OLD_PASSWORD, NEW_PASSWORD).andReturn().getResponse().getStatus();
    }

    private org.springframework.test.web.servlet.ResultActions passwordChange(String token, String current, String next)
            throws Exception {
        return mockMvc.perform(put("/users/me/password").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(passwordBody(current, next)));
    }

    private User createUser(String requestedUsername, GuildRole role, UserStatus status, String password) {
        String username = requestedUsername + "-" + UUID.randomUUID().toString().substring(0, 8);
        return createUserWithUsername(username, role, status, password);
    }

    private User createUserWithUsername(String username, GuildRole role, UserStatus status, String password) {
        Guild guild = guildRepository.saveAndFlush(Guild.builder().name("Password guild " + UUID.randomUUID())
                .serverName("Password server").build());
        guildIds.add(guild.getId());
        User user = userRepository.saveAndFlush(User.builder().username(username).email(username + "@example.test")
                .password(password == null ? null : passwordEncoder.encode(password)).guildRole(role)
                .status(status).guild(guild).build());
        userIds.add(user.getId());
        return user;
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String jwtUtilTestToken(User user, boolean withClaims) {
        if (withClaims) {
            return jwtUtil.generateToken(new CustomUserDetails(User.builder().id(user.getId()).username(user.getUsername())
                    .email(user.getEmail()).guild(user.getGuild()).guildRole(user.getGuildRole()).status(user.getStatus())
                    .credentialVersion(99L).build()), List.of(user.getGuildRole().name()));
        }
        return io.jsonwebtoken.Jwts.builder().setSubject(user.getUsername()).setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "password_change_test_secret_32_bytes_minimum".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        io.jsonwebtoken.SignatureAlgorithm.HS256).compact();
    }

    private String passwordBody(String current, String next) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of("currentPassword", current, "newPassword", next));
    }

    private String loginBody(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of("username", username, "password", password));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
