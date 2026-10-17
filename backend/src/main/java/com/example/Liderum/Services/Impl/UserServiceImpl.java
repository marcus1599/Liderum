package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Services.UserService;
import com.example.Liderum.Services.UserActivationService;
import com.example.Liderum.Repository.UserActivationTokenRepository;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.UserCreateRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import com.example.Liderum.dto.UserRoleUpdateRequestDTO;
import com.example.Liderum.dto.UserActivationResponseDTO;
import com.example.Liderum.dto.AdminUserCreateRequestDTO;
import com.example.Liderum.dto.UserStatusUpdateRequestDTO;
import com.example.Liderum.exceptions.UserStatusConflictException;
import com.example.Liderum.Enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final UserActivationService activationService;
    private final UserActivationTokenRepository activationTokenRepository;
    private final TeamRepository teamRepository;
    private final EntityManager entityManager;

    @Override
    public UserResponseDTO create(UserCreateRequestDTO dto) {
        AdminUserCreateRequestDTO admin = new AdminUserCreateRequestDTO(); admin.setUsername(dto.getUsername()); admin.setEmail(dto.getEmail()); admin.setRole(dto.getRole());
        return createWithActivation(admin).getUser();
    }

    @Override
    public UserActivationResponseDTO createWithActivation(UserCreateRequestDTO dto) {
        AdminUserCreateRequestDTO admin = new AdminUserCreateRequestDTO(); admin.setUsername(dto.getUsername()); admin.setEmail(dto.getEmail()); admin.setRole(dto.getRole());
        return createWithActivation(admin);
    }

    @Override
    public UserActivationResponseDTO createWithActivation(AdminUserCreateRequestDTO dto) {
        User actor = tenantService.getCurrentUser();
        assertCanCreate(actor.getGuildRole(), dto.getRole());
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(null)
                .guildRole(dto.getRole())
                .status(UserStatus.PENDING)
                .guild(actor.getGuild())
                .build();

        user = userRepository.save(user);

        return activationService.createActivation(user);
    }

    @Override
    @Transactional
    public UserActivationResponseDTO regenerateActivation(Long id) {
        User actor = tenantService.getCurrentUser();
        User target = findUserInCurrentGuild(id, actor.getGuild().getId());
        assertCanManageTarget(actor.getGuildRole(), target.getGuildRole(), null);
        if (target.getStatus() != UserStatus.PENDING) {
            throw new EntityNotFoundException("Activation unavailable");
        }
        return activationService.createActivation(target);
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
        lockMarechals(guildId);
        User target = findUserForUpdate(id, guildId);

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
        lockMarechals(guildId);
        User user = findUserForUpdate(id, guildId);

        assertCanManageTarget(actor.getGuildRole(), user.getGuildRole(), null);
        assertLastMarechalIsPreserved(user, null, guildId);
        activationTokenRepository.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateStatus(Long id, UserStatusUpdateRequestDTO dto) {
        User actor = tenantService.getCurrentUser();
        Long guildId = actor.getGuild().getId();
        lockMarechals(guildId);
        User target = findUserForUpdate(id, guildId);
        UserStatus requested = dto.getStatus();

        assertCanManageTarget(actor.getGuildRole(), target.getGuildRole(), null);
        if (actor.getId().equals(target.getId())) {
            throw new AccessDeniedException("Users cannot change their own status.");
        }
        if (target.getStatus() != UserStatus.ACTIVE && target.getStatus() != UserStatus.DISABLED) {
            throw new UserStatusConflictException();
        }
        if (requested == target.getStatus()) {
            throw new UserStatusConflictException();
        }
        if (requested == UserStatus.PENDING) {
            throw new UserStatusConflictException();
        }
        if (requested == UserStatus.DISABLED) {
            if (teamRepository.existsByGuildIdAndLeaderId(guildId, target.getId())) {
                throw new UserStatusConflictException();
            }
            assertLastActiveMarechalIsPreserved(target, requested, guildId);
            target.setCredentialVersion(target.getCredentialVersion() + 1);
        } else if (target.getPassword() == null || target.getPassword().isBlank()) {
            throw new UserStatusConflictException();
        }

        target.setStatus(requested);
        return toDTO(userRepository.save(target));
    }

    private User findUserInCurrentGuild(Long id, Long guildId) {
        return userRepository.findByIdAndGuildId(id, guildId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private User findUserForUpdate(Long id, Long guildId) {
        User user = userRepository.findByIdAndGuildIdForUpdate(id, guildId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        return user;
    }

    private void lockMarechals(Long guildId) {
        userRepository.findAllByGuildIdAndGuildRoleOrderById(guildId, GuildRole.MARECHAL);
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
                && target.getStatus() == UserStatus.ACTIVE
                && newRole != GuildRole.MARECHAL
                && userRepository.countByGuildIdAndGuildRoleAndStatus(guildId, GuildRole.MARECHAL, UserStatus.ACTIVE) <= 1) {
            throw new AccessDeniedException("A guild must retain at least one MARECHAL.");
        }
    }

    private void assertLastActiveMarechalIsPreserved(User target, UserStatus newStatus, Long guildId) {
        if (target.getGuildRole() == GuildRole.MARECHAL
                && target.getStatus() == UserStatus.ACTIVE
                && newStatus != UserStatus.ACTIVE
                && userRepository.countByGuildIdAndGuildRoleAndStatus(guildId, GuildRole.MARECHAL, UserStatus.ACTIVE) <= 1) {
            throw new AccessDeniedException("A guild must retain at least one active MARECHAL.");
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
        dto.setStatus(user.getStatus());
        return dto;
    }
}
