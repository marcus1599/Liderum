package com.example.Liderum.Controllers;

import com.example.Liderum.Services.UserService;
import com.example.Liderum.Services.PasswordChangeService;
import com.example.Liderum.dto.PasswordChangeRequestDTO;
import com.example.Liderum.dto.UserCreateRequestDTO;
import com.example.Liderum.dto.UserRoleUpdateRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import com.example.Liderum.dto.UserActivationResponseDTO;
import com.example.Liderum.dto.AdminUserCreateRequestDTO;
import com.example.Liderum.dto.UserStatusUpdateRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final UserService userService;
    private final PasswordChangeService passwordChangeService;

    @PutMapping("/me/password")
    @Operation(summary = "Alterar a própria senha")
    public ResponseEntity<Void> changeOwnPassword(
            @RequestBody @jakarta.validation.Valid PasswordChangeRequestDTO request) {
        passwordChangeService.changeCurrentUserPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Criar usuário")
    public ResponseEntity<UserActivationResponseDTO> create(@RequestBody @jakarta.validation.Valid AdminUserCreateRequestDTO request) {
        return ResponseEntity.ok(userService.createWithActivation(request));
    }

    @PostMapping("/{id}/activation")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Regenerar link de ativação")
    public ResponseEntity<UserActivationResponseDTO> regenerateActivation(@PathVariable Long id) {
        return ResponseEntity.ok(userService.regenerateActivation(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar o próprio perfil")
    public ResponseEntity<UserResponseDTO> findCurrentUser() {
        return ResponseEntity.ok(userService.findCurrentUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Alterar role de usuário")
    public ResponseEntity<UserResponseDTO> updateRole(@PathVariable Long id,
            @RequestBody @jakarta.validation.Valid UserRoleUpdateRequestDTO request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Alterar status de usuário")
    public ResponseEntity<UserResponseDTO> updateStatus(@PathVariable Long id,
            @RequestBody @jakarta.validation.Valid UserStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(userService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARECHAL', 'GENERAL')")
    @Operation(summary = "Deletar usuário por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
