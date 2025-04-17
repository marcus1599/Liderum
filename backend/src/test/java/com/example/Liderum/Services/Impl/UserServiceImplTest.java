package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.dto.UserRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void shouldCreateUserSuccessfully() {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("marcus");
        request.setEmail("marcus@example.com");
        request.setPassword("123456");

        User user = User.builder()
                .id(1L)
                .username("marcus")
                .email("marcus@example.com")
                .password("123456")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO response = userService.create(request);

        assertNotNull(response);
        assertEquals("marcus", response.getUsername());
        assertEquals("marcus@example.com", response.getEmail());
    }

    @Test
    public void shouldFindAllUsers() {
        User user = User.builder()
                .id(1L)
                .username("marcus")
                .email("marcus@example.com")
                .password("123456")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDTO> users = userService.findAll();

        assertEquals(1, users.size());
        assertEquals("marcus", users.get(0).getUsername());
    }

    @Test
    public void shouldFindUserById() {
        User user = User.builder()
                .id(1L)
                .username("marcus")
                .email("marcus@example.com")
                .password("123456")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findById(1L);

        assertNotNull(response);
        assertEquals("marcus", response.getUsername());
    }

    @Test
    public void shouldDeleteUser() {
        Long id = 1L;

        doNothing().when(userRepository).deleteById(id);

        userService.delete(id);

        verify(userRepository, times(1)).deleteById(id);
    }
}
