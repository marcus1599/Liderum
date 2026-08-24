package com.example.Liderum.Repository;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;

import java.util.Optional;
import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndGuildId(Long id, Long guildId);
    List<User> findAllByGuildId(Long guildId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<User> findAllByGuildIdAndGuildRole(Long guildId, GuildRole guildRole);
}
