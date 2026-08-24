package com.example.Liderum.Security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class JwtFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateValidTokenWithoutLoggingSensitiveData(CapturedOutput output) throws Exception {
        String token = "sensitive.jwt.token";
        String username = "sensitive-user";
        List<String> roles = List.of("MARECHAL");
        JwtUtil jwtUtil = mock(JwtUtil.class);
        var userDetailsService = mock(org.springframework.security.core.userdetails.UserDetailsService.class);
        UserDetails userDetails = mock(UserDetails.class);
        var filterChain = mock(jakarta.servlet.FilterChain.class);
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MARECHAL"))).when(userDetails).getAuthorities();

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MARECHAL");
        assertThat(output.getOut())
                .doesNotContain("Authorization")
                .doesNotContain("Bearer " + token)
                .doesNotContain(token)
                .doesNotContain(username)
                .doesNotContain("MARECHAL");
        verify(filterChain).doFilter(request, response);
    }
}
