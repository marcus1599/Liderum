package com.example.Liderum.Repository;
import com.example.Liderum.Entities.UserActivationToken;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface UserActivationTokenRepository extends JpaRepository<UserActivationToken,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<UserActivationToken> findByTokenHash(String tokenHash);
 java.util.List<UserActivationToken> findAllByUserIdAndUsedAtIsNullAndRevokedAtIsNull(Long userId);
 void deleteAllByUserId(Long userId);
}
