package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.GuildRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.GuildOnboardingService;
import com.example.Liderum.dto.GuildRegistrationRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuildOnboardingServiceImpl implements GuildOnboardingService {
    private final GuildRepository guildRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO register(GuildRegistrationRequestDTO request) {
        Guild guild = guildRepository.save(Guild.builder()
                .name(request.getGuildName()).serverName(request.getServerName()).build());
        User admin = userRepository.save(User.builder()
                .username(request.getUsername()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .guildRole(GuildRole.MARECHAL).guild(guild).build());
        UserResponseDTO response = new UserResponseDTO();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        return response;
    }
}
