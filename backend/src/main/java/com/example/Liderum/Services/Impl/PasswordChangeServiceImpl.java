package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.UserStatus;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Security.CustomUserDetails;
import com.example.Liderum.Services.PasswordChangeService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.PasswordChangeRequestDTO;
import com.example.Liderum.exceptions.PasswordChangeException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordChangeServiceImpl implements PasswordChangeService {

    private final UserRepository userRepository;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void changeCurrentUserPassword(PasswordChangeRequestDTO request) {
        User current = tenantService.getCurrentUser();
        User user = userRepository.findByIdForPasswordChange(current.getId())
                .orElseThrow(() -> new PasswordChangeException(PasswordChangeException.Code.SESSION_EXPIRED));
        // TenantService may already have loaded this entity into the request's persistence context
        // before the row lock was acquired. Refresh after locking so a concurrent password change
        // cannot validate against a stale password/version snapshot.
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof CustomUserDetails principal)
                || !Objects.equals(principal.getUser().getId(), user.getId())
                || principal.getUser().getCredentialVersion() != user.getCredentialVersion()
                || user.getStatus() != UserStatus.ACTIVE) {
            throw new PasswordChangeException(PasswordChangeException.Code.SESSION_EXPIRED);
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new PasswordChangeException(PasswordChangeException.Code.INVALID_PASSWORD_REQUEST);
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordChangeException(PasswordChangeException.Code.CURRENT_PASSWORD_INVALID);
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new PasswordChangeException(PasswordChangeException.Code.PASSWORD_UNCHANGED);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialVersion(user.getCredentialVersion() + 1);
        userRepository.save(user);
    }
}
