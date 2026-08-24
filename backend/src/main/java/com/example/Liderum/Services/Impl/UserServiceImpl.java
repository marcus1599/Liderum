package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.UserService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.UserRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .guildRole(dto.getGuildRole())
                .guild(tenantService.getCurrentGuild())
                .build();

        user = userRepository.save(user);

        return toDTO(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {
        return userRepository.findAllByGuildId(tenantService.getCurrentGuildId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findByIdAndGuildId(id, tenantService.getCurrentGuildId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return toDTO(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findByIdAndGuildId(id, tenantService.getCurrentGuildId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
