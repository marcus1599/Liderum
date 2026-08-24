package com.example.Liderum.config;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=test_only_dev_profile_bootstrap_secret",
        "spring.datasource.url=jdbc:h2:mem:bootstrap_dev_profile;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("dev")
class DataInitializerDevProfileIntegrationTest {

    @Autowired
    private GuildRepository guildRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void seedsDemoDataOnlyWhenDevProfileIsActive() {
        User demoAdmin = userRepository.findByUsername("admin").orElseThrow();

        assertThat(guildRepository.count()).isEqualTo(1);
        assertThat(demoAdmin.getGuildRole()).isEqualTo(GuildRole.MARECHAL);
        assertThat(demoAdmin.getGuild()).isNotNull();
        assertThat(memberRepository.count()).isEqualTo(50);
        assertThat(eventRepository.count()).isEqualTo(3);
    }
}
