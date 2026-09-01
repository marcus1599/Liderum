package com.example.Liderum.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RegistrationRateLimitFilter extends OncePerRequestFilter {
    private static final String REGISTRATION_PATH = "/auth/register-guild";
    private static final String CLOUDFLARE_CLIENT_IP_HEADER = "CF-Connecting-IP";
    private final RegistrationRateLimiter rateLimiter;

    @Value("${liderum.registration.rate-limit.trust-cloudflare-client-ip:false}")
    private boolean trustCloudflareClientIp;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !REGISTRATION_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!rateLimiter.tryAcquire(resolveClientKey(request))) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String cloudflareClientIp = request.getHeader(CLOUDFLARE_CLIENT_IP_HEADER);
        return !trustCloudflareClientIp || cloudflareClientIp == null || cloudflareClientIp.isBlank()
                ? request.getRemoteAddr()
                : cloudflareClientIp.trim();
    }
}
