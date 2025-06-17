package com.example.Liderum.Controllers;

import com.example.Liderum.dto.AuthRequestDTO;
import com.example.Liderum.dto.AuthResponseDTO;
import com.example.Liderum.Security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
                
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Extrair todas as roles (sem prefixo ROLE_)
        List<String> roles = userDetails.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toList());

        // Gerar token com todas as roles
        String token = jwtUtil.generateToken(authentication.getName(), roles);

        return ResponseEntity.ok(new AuthResponseDTO(token, String.join(",", roles))); // opcionalmente envia as roles no response
    }
}
