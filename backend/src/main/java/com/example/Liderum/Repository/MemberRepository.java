package com.example.Liderum.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Liderum.Entities.Member;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findAllByGuildId(Long guildId);

    Optional<Member> findByIdAndGuildId(Long id, Long guildId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id and m.guild.id = :guildId")
    Optional<Member> findByIdAndGuildIdForUpdate(@Param("id") Long id, @Param("guildId") Long guildId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.team.id = :teamId and m.guild.id = :guildId order by m.id")
    List<Member> findAllByTeamAndGuildForUpdate(@Param("teamId") Long teamId, @Param("guildId") Long guildId);

    @Query("select m from Member m where m.team.id = :teamId and m.guild.id = :guildId order by m.id")
    List<Member> findAllByTeamAndGuild(@Param("teamId") Long teamId, @Param("guildId") Long guildId);

    @Query("select m from Member m where m.id = :memberId and m.team.id = :teamId and m.guild.id = :guildId")
    Optional<Member> findAssignedMember(@Param("memberId") Long memberId, @Param("teamId") Long teamId, @Param("guildId") Long guildId);
}
