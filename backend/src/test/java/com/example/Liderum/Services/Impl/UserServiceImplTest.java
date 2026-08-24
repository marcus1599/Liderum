package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.UserCreateRequestDTO;
import com.example.Liderum.dto.UserRoleUpdateRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock TenantService tenantService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserServiceImpl userService;

    @Test
    void shouldCreateUserWithEncodedPasswordAndCurrentGuild() {
        Guild guild = Guild.builder().id(7L).name("Guild").build();
        UserCreateRequestDTO request = request(GuildRole.SOLDADO);
        when(tenantService.getCurrentUser()).thenReturn(User.builder()
                .id(9L).guild(guild).guildRole(GuildRole.MARECHAL).build());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserResponseDTO response = userService.create(request);
        assertThat(response.getUsername()).isEqualTo("marcus");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user -> user.getGuild() == guild
                && user.getGuildRole() == GuildRole.SOLDADO && user.getPassword().equals("encoded")));
    }

    @Test
    void shouldListOnlyCurrentGuildUsers() {
        when(tenantService.getCurrentGuildId()).thenReturn(7L);
        when(userRepository.findAllByGuildId(7L)).thenReturn(List.of(user()));
        assertThat(userService.findAll()).hasSize(1);
        verify(userRepository).findAllByGuildId(7L);
        verify(userRepository, never()).findAll();
    }

    @Test
    void shouldFindAndDeleteOnlyCurrentGuildUser() {
        User user = user();
        when(tenantService.getCurrentGuildId()).thenReturn(7L);
        when(tenantService.getCurrentUser()).thenReturn(User.builder()
                .id(2L).guild(Guild.builder().id(7L).build()).guildRole(GuildRole.MARECHAL).build());
        when(userRepository.findByIdAndGuildId(1L, 7L)).thenReturn(Optional.of(user));
        assertThat(userService.findById(1L).getUsername()).isEqualTo("marcus");
        userService.delete(1L);
        verify(userRepository, times(2)).findByIdAndGuildId(1L, 7L);
        verify(userRepository).delete(user);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldRejectGeneralCreatingGeneral() {
        when(tenantService.getCurrentUser()).thenReturn(user(GuildRole.GENERAL));

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> userService.create(request(GuildRole.GENERAL)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldPreserveLastMarechalWhenChangingRole() {
        User marechal = user(GuildRole.MARECHAL);
        when(tenantService.getCurrentUser()).thenReturn(marechal);
        when(userRepository.findByIdAndGuildId(1L, 7L)).thenReturn(Optional.of(marechal));
        when(userRepository.findAllByGuildIdAndGuildRole(7L, GuildRole.MARECHAL)).thenReturn(List.of(marechal));
        UserRoleUpdateRequestDTO request = new UserRoleUpdateRequestDTO();
        request.setRole(GuildRole.GENERAL);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> userService.updateRole(1L, request));
        verify(userRepository, never()).save(marechal);
    }

    private UserCreateRequestDTO request(GuildRole role) {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setUsername("marcus"); request.setEmail("marcus@example.com");
        request.setPassword("password123"); request.setRole(role);
        return request;
    }

    private User user() {
        return user(GuildRole.SOLDADO);
    }

    private User user(GuildRole role) {
        return User.builder().id(1L).username("marcus").email("marcus@example.com")
                .password("encoded").guildRole(role)
                .guild(Guild.builder().id(7L).build()).build();
    }
}
