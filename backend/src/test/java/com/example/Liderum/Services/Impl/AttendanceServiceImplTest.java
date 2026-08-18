package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Entities.Event;
import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Enums.AttendanceStatus;
import com.example.Liderum.Repository.AttendanceRepository;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.AttendanceRequestDTO;
import com.example.Liderum.dto.AttendanceResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Guild guild;
    private Member member;
    private Event event;

    @BeforeEach
    public void setup() {
        guild = Guild.builder().id(1L).name("Guilda Teste").build();

        lenient().when(tenantService.getCurrentGuild()).thenReturn(guild);
        lenient().when(tenantService.getCurrentGuildId()).thenReturn(1L);

        member = Member.builder().id(1L).nickname("Marcus").guild(guild).build();
        event = Event.builder().id(1L).name("Evento RPG").guild(guild).build();
    }

    @Test
    public void shouldCreateAttendanceSuccessfully() {
        AttendanceRequestDTO dto = new AttendanceRequestDTO();
        dto.setMemberId(1L);
        dto.setEventId(1L);
        dto.setStatus(AttendanceStatus.PRESENTE);

        Attendance attendance = Attendance.builder()
                .id(1L)
                .member(member)
                .event(event)
                .status(AttendanceStatus.PRESENTE)
                .build();

        when(memberRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(member));
        when(eventRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(event));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceResponseDTO response = attendanceService.create(dto);

        assertNotNull(response);
        assertEquals(AttendanceStatus.PRESENTE, response.getStatus());
        assertEquals("Marcus", response.getMemberName());
        assertEquals("Evento RPG", response.getEventName());
    }

    @Test
    public void shouldFindAllAttendances() {
        Attendance attendance = Attendance.builder()
                .id(1L)
                .member(member)
                .event(event)
                .status(AttendanceStatus.FALTOU)
                .build();

        when(attendanceRepository.findAllByEventGuildId(1L)).thenReturn(List.of(attendance));

        List<AttendanceResponseDTO> list = attendanceService.findAll();

        assertEquals(1, list.size());
        assertEquals(AttendanceStatus.FALTOU, list.get(0).getStatus());
    }

    @Test
    public void shouldFindById() {
        Attendance attendance = Attendance.builder()
                .id(1L)
                .member(member)
                .event(event)
                .status(AttendanceStatus.JUSTIFICOU)
                .build();

        when(attendanceRepository.findByIdAndEventGuildId(1L, 1L)).thenReturn(Optional.of(attendance));

        AttendanceResponseDTO dto = attendanceService.findById(1L);

        assertNotNull(dto);
        assertEquals(AttendanceStatus.JUSTIFICOU, dto.getStatus());
    }

    @Test
    public void shouldUpdateAttendance() {
        AttendanceRequestDTO dto = new AttendanceRequestDTO();
        dto.setMemberId(1L);
        dto.setEventId(1L);
        dto.setStatus(AttendanceStatus.PRESENTE);

        Attendance existing = Attendance.builder()
                .id(1L)
                .member(member)
                .event(event)
                .status(AttendanceStatus.FALTOU)
                .build();

        Attendance updated = Attendance.builder()
                .id(1L)
                .member(member)
                .event(event)
                .status(AttendanceStatus.PRESENTE)
                .build();

        when(attendanceRepository.findByIdAndEventGuildId(1L, 1L)).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(updated);

        AttendanceResponseDTO response = attendanceService.update(1L, dto);

        assertEquals(AttendanceStatus.PRESENTE, response.getStatus());
    }

    @Test
    public void shouldDeleteAttendance() {
        Attendance attendance = Attendance.builder()
                .id(1L)
                .member(member)
                .event(event)
                .status(AttendanceStatus.PRESENTE)
                .build();

        when(attendanceRepository.findByIdAndEventGuildId(1L, 1L)).thenReturn(Optional.of(attendance));
        doNothing().when(attendanceRepository).delete(attendance);

        attendanceService.delete(1L);

        verify(attendanceRepository, times(1)).delete(attendance);
    }
}
