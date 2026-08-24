package com.example.Liderum.config;

import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test_only_prod_profile_bootstrap_secret",
        "spring.datasource.url=jdbc:h2:mem:bootstrap_prod_profile;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "liderum.cors.allowed-origins="
})
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ProductionConfigurationIntegrationTest {

    @Autowired
    private GuildRepository guildRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void doesNotSeedDemoDataOrAllowCrossOriginRequestsInProdWithoutOrigins() throws Exception {
        assertThat(guildRepository.count()).isZero();
        assertThat(userRepository.count()).isZero();
        assertThat(memberRepository.count()).isZero();
        assertThat(eventRepository.count()).isZero();

        mockMvc.perform(options("/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
