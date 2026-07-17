package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Entities.Event;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Enums.AttendanceStatus;
import com.example.Liderum.Repository.AttendanceRepository;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Services.AttendanceService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.AttendanceRequestDTO;
import com.example.Liderum.dto.AttendanceResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final TenantService tenantService;

    @Override
    public AttendanceResponseDTO create(AttendanceRequestDTO dto) {
        Long guildId = tenantService.getCurrentGuildId();
        Member member = memberRepository.findByIdAndGuildId(dto.getMemberId(), guildId)
                .orElseThrow(() -> new EntityNotFoundException("Membro nao encontrado"));
        Event event = eventRepository.findByIdAndGuildId(dto.getEventId(), guildId)
                .orElseThrow(() -> new EntityNotFoundException("Evento nao encontrado"));

        Attendance attendance = Attendance.builder()
                .member(member)
                .event(event)
                .status(dto.getStatus())
                .build();

        return toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public List<AttendanceResponseDTO> findAll() {
        return attendanceRepository.findAllByEventGuildId(tenantService.getCurrentGuildId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceResponseDTO findById(Long id) {
        return toDTO(findAttendanceInCurrentGuild(id));
    }

    @Override
    public AttendanceResponseDTO update(Long id, AttendanceRequestDTO dto) {
        Attendance attendance = findAttendanceInCurrentGuild(id);
        attendance.setStatus(dto.getStatus());

        return toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public void delete(Long id) {
        attendanceRepository.delete(findAttendanceInCurrentGuild(id));
    }

    @Override
    public List<Long> findMembersWithConsecutiveAbsences(int threshold) {
        Long guildId = tenantService.getCurrentGuildId();
        List<Long> eventIds = attendanceRepository.findAllByEventGuildId(guildId).stream()
                .map(attendance -> attendance.getEvent().getId())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (eventIds.size() < threshold) {
            return List.of();
        }

        List<Long> lastEventIds = eventIds.subList(eventIds.size() - threshold, eventIds.size());
        List<Long> absents = attendanceRepository.findAllByStatusAndEventIdsAndGuildId(
                AttendanceStatus.FALTOU,
                lastEventIds,
                guildId
        );

        return absents.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() == threshold)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }

    private Attendance findAttendanceInCurrentGuild(Long id) {
        return attendanceRepository.findByIdAndEventGuildId(id, tenantService.getCurrentGuildId())
                .orElseThrow(() -> new EntityNotFoundException("Presenca nao encontrada"));
    }

    private AttendanceResponseDTO toDTO(Attendance attendance) {
        return AttendanceResponseDTO.builder()
                .id(attendance.getId())
                .memberId(attendance.getMember().getId())
                .eventId(attendance.getEvent().getId())
                .eventName(attendance.getEvent().getName())
                .memberName(attendance.getMember().getNickname())
                .status(attendance.getStatus())
                .build();
    }
}
