package com.example.Liderum.Services;

import com.example.Liderum.dto.UserCreateRequestDTO;
import com.example.Liderum.dto.UserRoleUpdateRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO create(UserCreateRequestDTO dto);
    List<UserResponseDTO> findAll();
    UserResponseDTO findById(Long id);
    UserResponseDTO findCurrentUser();
    UserResponseDTO updateRole(Long id, UserRoleUpdateRequestDTO dto);
    void delete(Long id);
}
