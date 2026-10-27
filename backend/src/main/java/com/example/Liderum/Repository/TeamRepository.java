package com.example.Liderum.Repository;

import com.example.Liderum.Entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByGuildId(Long guildId);

    Optional<Team> findByIdAndGuildId(Long id, Long guildId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Team t where t.id = :id and t.guild.id = :guildId")
    Optional<Team> findByIdAndGuildIdForUpdate(@Param("id") Long id, @Param("guildId") Long guildId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Team t where t.guild.id = :guildId and t.id in :ids order by t.id")
    List<Team> findAllByIdsAndGuildIdForUpdate(@Param("ids") Collection<Long> ids, @Param("guildId") Long guildId);
}
