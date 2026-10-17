package com.example.Liderum.Repository;

import com.example.Liderum.Entities.User;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Enums.UserStatus;

import java.util.Optional;
import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndGuildId(Long id, Long guildId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id and u.guild.id = :guildId")
    Optional<User> findByIdAndGuildIdForUpdate(@Param("id") Long id, @Param("guildId") Long guildId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForPasswordChange(@Param("id") Long id);
    List<User> findAllByGuildId(Long guildId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<User> findAllByGuildIdAndGuildRole(Long guildId, GuildRole guildRole);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<User> findAllByGuildIdAndGuildRoleOrderById(Long guildId, GuildRole guildRole);
    long countByGuildIdAndGuildRoleAndStatus(Long guildId, GuildRole guildRole, UserStatus status);
}
