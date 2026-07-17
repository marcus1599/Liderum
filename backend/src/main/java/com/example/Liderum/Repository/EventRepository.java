package com.example.Liderum.Repository;

import com.example.Liderum.Entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByGuildId(Long guildId);

    Optional<Event> findByIdAndGuildId(Long id, Long guildId);
}
