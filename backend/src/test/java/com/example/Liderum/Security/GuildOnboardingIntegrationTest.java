package com.example.Liderum.Security;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=test_only_guild_onboarding_secret")
@AutoConfigureMockMvc
@Transactional
class GuildOnboardingIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterGuildAndFirstMarechalWithHashedPassword() throws Exception {
        String body = """
                {"guildName":"Guild Onboarding","serverName":"Server A","username":"owner-onboarding","email":"owner-onboarding@example.com","password":"password123"}
                """;

        String response = mockMvc.perform(post("/auth/register-guild")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("owner-onboarding"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        JsonNode registered = objectMapper.readTree(response);
        User owner = userRepository.findById(registered.get("id").asLong()).orElseThrow();
        assertThat(owner.getGuild()).isNotNull();
        assertThat(owner.getGuildRole().name()).isEqualTo("MARECHAL");
        assertThat(owner.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", owner.getPassword())).isTrue();
    }

    @Test
    void shouldAllowOwnerToProvisionUserOnlyInsideOwnGuild() throws Exception {
        String token = registerAndLogin("owner-provision", "owner-provision@example.com", "Guild Provision");
        String userBody = """
                {"username":"member-provision","email":"member-provision@example.com","password":"password123","role":"SOLDADO"}
                """;

        mockMvc.perform(post("/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(userBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldRejectUserAdministrationWithoutMarechalRole() throws Exception {
        String token = registerAndLogin("owner-rbac", "owner-rbac@example.com", "Guild RBAC");
        String userBody = """
                {"username":"soldier-rbac","email":"soldier-rbac@example.com","password":"password123","role":"SOLDADO"}
                """;
        mockMvc.perform(post("/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(userBody))
                .andExpect(status().isOk());

        String soldierToken = login("soldier-rbac", "password123");
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + soldierToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCrossTenantUserRead() throws Exception {
        String tokenA = registerAndLogin("owner-a", "owner-a@example.com", "Guild A onboarding");
        String tokenB = registerAndLogin("owner-b", "owner-b@example.com", "Guild B onboarding");
        String body = """
                {"username":"member-b","email":"member-b@example.com","password":"password123","role":"SOLDADO"}
                """;
        String created = mockMvc.perform(post("/users").header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long userBId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/users/" + userBId).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String username, String email, String guildName) throws Exception {
        String body = """
                {"guildName":"%s","serverName":"Server","username":"%s","email":"%s","password":"password123"}
                """.formatted(guildName, username, email);
        mockMvc.perform(post("/auth/register-guild").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        return login(username, "password123");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
