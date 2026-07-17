package com.example.Liderum.Repository;

import com.example.Liderum.Entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllByGuildId(Long guildId);

    Optional<Team> findByIdAndGuildId(Long id, Long guildId);
}
