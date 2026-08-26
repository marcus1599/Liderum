package com.example.Liderum.Security;

import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest(properties = {
        "jwt.secret=test_only_registration_rate_limit_secret",
        "liderum.registration.rate-limit.limit=5",
        "liderum.registration.rate-limit.window=15m",
        "liderum.registration.rate-limit.max-clients=100"
})
@AutoConfigureMockMvc
@ActiveProfiles("default")
class RegistrationRateLimitIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired GuildRepository guildRepository;
    @Autowired UserRepository userRepository;
    @Autowired ObjectMapper objectMapper;

    @Test
    void blocksSixthAttemptAndDoesNotPersistIt() throws Exception {
        String ip = "198.51.100.10";
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(register(ip, "limited-" + i)).andExpect(status().isCreated());
        }
        long guildsBefore = guildRepository.count();
        long usersBefore = userRepository.count();

        mockMvc.perform(register(ip, "limited-blocked"))
                .andExpect(status().isTooManyRequests())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(not(containsString("password"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(not(containsString("198.51.100.10"))));

        assertThat(guildRepository.count()).isEqualTo(guildsBefore);
        assertThat(userRepository.count()).isEqualTo(usersBefore);
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"limited-0\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
        String token = objectMapper.readTree(mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"limited-0\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void forwardedHeadersDoNotCreateAnotherBucket() throws Exception {
        String ip = "198.51.100.11";
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(register(ip, "forwarded-" + i)
                    .header("X-Forwarded-For", "203.0.113." + i)
                    .header("Forwarded", "for=203.0.113." + i))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(register(ip, "forwarded-blocked").header("X-Forwarded-For", "203.0.113.99"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void anotherRemoteAddressHasIndependentQuota() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(register("198.51.100.12", "first-ip-" + i)).andExpect(status().isCreated());
        }
        mockMvc.perform(register("198.51.100.13", "second-ip-0"))
                .andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder register(String ip, String suffix) {
        return post("/auth/register-guild")
                .with(request -> { request.setRemoteAddr(ip); return request; })
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"guildName\":\"Guild " + suffix + "\",\"serverName\":\"Server\",\"username\":\"" + suffix + "\",\"email\":\"" + suffix + "@example.test\",\"password\":\"password123\"}");
    }
}
