package com.example.Liderum.Controllers;

import com.example.Liderum.Services.UserService;
import com.example.Liderum.dto.UserRequestDTO;
import com.example.Liderum.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Criar usuário")
    public ResponseEntity<UserResponseDTO> create(@RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
