package com.example.Liderum.Config;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.findAll().isEmpty()) {
                User admin = User.builder()
                .username("admin")
                .email("admin@email.com") 
                .password(passwordEncoder.encode("admin123"))
                .guildRole(GuildRole.MARSHAL)
                .build();
        
        User user = User.builder()
                .username("usuario")
                .email("usuario@email.com") 
                .password(passwordEncoder.encode("senha123"))
                .guildRole(GuildRole.SOLDIER)
                .build();
        

                userRepository.save(admin);
                userRepository.save(user);

                System.out.println("Usuários padrão criados.");
            }
        };
    }
}
