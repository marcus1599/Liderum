package com.example.Liderum.Services;

import com.example.Liderum.dto.UserCreateRequestDTO;
import com.example.Liderum.dto.UserRoleUpdateRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO create(UserCreateRequestDTO dto);
    com.example.Liderum.dto.UserActivationResponseDTO createWithActivation(UserCreateRequestDTO dto);
    com.example.Liderum.dto.UserActivationResponseDTO createWithActivation(com.example.Liderum.dto.AdminUserCreateRequestDTO dto);
    com.example.Liderum.dto.UserActivationResponseDTO regenerateActivation(Long id);
    List<UserResponseDTO> findAll();
    UserResponseDTO findById(Long id);
    UserResponseDTO findCurrentUser();
    UserResponseDTO updateRole(Long id, UserRoleUpdateRequestDTO dto);
    void delete(Long id);
}
