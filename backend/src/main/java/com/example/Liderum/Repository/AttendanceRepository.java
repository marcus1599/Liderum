package com.example.Liderum.Repository;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Enums.AttendanceStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

   List<Attendance> findAllByEventGuildId(Long guildId);

   Optional<Attendance> findByIdAndEventGuildId(Long id, Long guildId);

   @Query("SELECT a.member.id FROM Attendance a WHERE a.status = :status AND a.event.id IN :eventIds")
List<Long> findAllByStatusAndEventIds(@Param("status") AttendanceStatus status, @Param("eventIds") List<Long> eventIds);

   @Query("SELECT a.member.id FROM Attendance a WHERE a.status = :status AND a.event.id IN :eventIds AND a.event.guild.id = :guildId")
List<Long> findAllByStatusAndEventIdsAndGuildId(
        @Param("status") AttendanceStatus status,
        @Param("eventIds") List<Long> eventIds,
        @Param("guildId") Long guildId);
}
