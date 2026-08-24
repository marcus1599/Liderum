package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.UserService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.UserCreateRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import com.example.Liderum.dto.UserRoleUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO create(UserCreateRequestDTO dto) {
        User actor = tenantService.getCurrentUser();
        assertCanCreate(actor.getGuildRole(), dto.getRole());
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .guildRole(dto.getRole())
                .guild(actor.getGuild())
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
    public UserResponseDTO findCurrentUser() {
        return toDTO(tenantService.getCurrentUser());
    }

    @Override
    @Transactional
    public UserResponseDTO updateRole(Long id, UserRoleUpdateRequestDTO dto) {
        User actor = tenantService.getCurrentUser();
        Long guildId = actor.getGuild().getId();
        User target = findUserInCurrentGuild(id, guildId);

        assertCanManageTarget(actor.getGuildRole(), target.getGuildRole(), dto.getRole());
        assertLastMarechalIsPreserved(target, dto.getRole(), guildId);

        target.setGuildRole(dto.getRole());
        return toDTO(userRepository.save(target));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User actor = tenantService.getCurrentUser();
        Long guildId = actor.getGuild().getId();
        User user = findUserInCurrentGuild(id, guildId);

        assertCanManageTarget(actor.getGuildRole(), user.getGuildRole(), null);
        assertLastMarechalIsPreserved(user, null, guildId);
        userRepository.delete(user);
    }

    private User findUserInCurrentGuild(Long id, Long guildId) {
        return userRepository.findByIdAndGuildId(id, guildId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void assertCanCreate(GuildRole actorRole, GuildRole targetRole) {
        if (actorRole == GuildRole.MARECHAL) {
            return;
        }
        if (actorRole == GuildRole.GENERAL && isBelowGeneral(targetRole)) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to create this role.");
    }

    private void assertCanManageTarget(GuildRole actorRole, GuildRole targetCurrentRole, GuildRole targetNewRole) {
        if (actorRole == GuildRole.MARECHAL) {
            return;
        }
        if (actorRole == GuildRole.GENERAL
                && isBelowGeneral(targetCurrentRole)
                && (targetNewRole == null || isBelowGeneral(targetNewRole))) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to manage this user.");
    }

    private void assertLastMarechalIsPreserved(User target, GuildRole newRole, Long guildId) {
        if (target.getGuildRole() == GuildRole.MARECHAL
                && newRole != GuildRole.MARECHAL
                && userRepository.findAllByGuildIdAndGuildRole(guildId, GuildRole.MARECHAL).size() <= 1) {
            throw new AccessDeniedException("A guild must retain at least one MARECHAL.");
        }
    }

    private boolean isBelowGeneral(GuildRole role) {
        return role == GuildRole.MAJOR || role == GuildRole.CAPITÃO || role == GuildRole.SOLDADO;
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setGuildRole(user.getGuildRole());
        return dto;
    }
}
