package com.example.Liderum.Security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("Token recebido: " + token);

            boolean valido = jwtUtil.validateToken(token);
            System.out.println("Token válido? " + valido);

            if (valido) {
                String username = jwtUtil.extractUsername(token);
                System.out.println("Username extraído: " + username);

                // Extrai as roles do token
                List<String> roles = jwtUtil.extractRoles(token);
                System.out.println("Roles extraídas: " + roles);

                // Mapeia as roles para SimpleGrantedAuthority com prefixo ROLE_
                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                var userDetails = userDetailsService.loadUserByUsername(username);

                var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Configura o contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Usuário autenticado no contexto de segurança");
            }
        } else {
            System.out.println("Token não encontrado ou não começa com Bearer");
        }

        filterChain.doFilter(request, response);
    }
}
