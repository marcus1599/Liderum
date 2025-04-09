package com.example.Liderum.Entities;

import com.example.Liderum.Enums.AttendanceStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Attendance {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Event event;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // PRESENTE, FALTOU, JUSTIFICOU
}
