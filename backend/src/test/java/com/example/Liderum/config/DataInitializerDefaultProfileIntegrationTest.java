package com.example.Liderum.config;

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
        "jwt.secret=test_only_default_profile_bootstrap_secret",
        "spring.datasource.url=jdbc:h2:mem:bootstrap_default_profile;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("default")
class DataInitializerDefaultProfileIntegrationTest {

    @Autowired
    private GuildRepository guildRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void doesNotSeedDemoDataWithoutDevProfile() {
        assertThat(guildRepository.count()).isZero();
        assertThat(userRepository.count()).isZero();
        assertThat(memberRepository.count()).isZero();
        assertThat(eventRepository.count()).isZero();
    }
}
