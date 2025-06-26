package com.example.Liderum.Config;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Event;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, MemberRepository memberRepository, EventRepository eventRepository) {
        return args -> {
            // Usuários padrão
            if (userRepository.findAll().isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@email.com")
                        .password(passwordEncoder.encode("admin123"))
                        .guildRole(GuildRole.MARECHAL)
                        .build();
                userRepository.save(admin);
            }

            // Membros mock
            if (memberRepository.findAll().isEmpty()) {
                List<Member> members = new ArrayList<>();
                for (int i = 1; i <= 50; i++) {
                    members.add(Member.builder()
                        .nickname("Membro" + i)
                        .phone("119999900" + String.format("%02d", i))
                        .guildRole(GuildRole.SOLDADO)
                        .rank("S")
                        .classe(Classe.values()[i % Classe.values().length])
                        .build());
                }
                memberRepository.saveAll(members);
            }

            // Eventos mock
            if (eventRepository.findAll().isEmpty()) {
                List<Event> events = List.of(
                    Event.builder().name("Evento Inicial").date(LocalDateTime.now().minusDays(7)).description("Primeiro evento").build(),
                    Event.builder().name("Evento Recente").date(LocalDateTime.now().minusDays(2)).description("Segundo evento").build(),
                    Event.builder().name("Evento Atual").date(LocalDateTime.now()).description("Terceiro evento").build()
                );
                eventRepository.saveAll(events);
            }
        };
    }
}
