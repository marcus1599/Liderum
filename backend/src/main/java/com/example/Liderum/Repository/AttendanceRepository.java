package com.example.Liderum.Repository;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Enums.AttendanceStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

   @Query("SELECT a.member.id FROM Attendance a WHERE a.status = :status AND a.event.id IN :eventIds")
List<Long> findAllByStatusAndEventIds(@Param("status") AttendanceStatus status, @Param("eventIds") List<Long> eventIds);
}
