package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Entities.Event;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Enums.AttendanceStatus;
import com.example.Liderum.Repository.AttendanceRepository;
import com.example.Liderum.Repository.EventRepository;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Services.AttendanceService;
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

    @Override
    public AttendanceResponseDTO create(AttendanceRequestDTO dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("Membro não encontrado"));
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Evento não encontrado"));

        Attendance attendance = Attendance.builder()
                .member(member)
                .event(event)
                .status(dto.getStatus())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return toDTO(saved);
    }

    @Override
    public List<AttendanceResponseDTO> findAll() {
        return attendanceRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceResponseDTO findById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Presença não encontrada"));
        return toDTO(attendance);
    }

    @Override
    public AttendanceResponseDTO update(Long id, AttendanceRequestDTO dto) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Presença não encontrada"));

        attendance.setStatus(dto.getStatus());
        return toDTO(attendanceRepository.save(attendance));
    }

    @Override
    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }
    @Override
    public List<Long> findMembersWithConsecutiveAbsences(int threshold) {
        // Busca todos os eventos ordenados por data
        List<Long> eventIds = attendanceRepository.findAll().stream()
            .map(a -> a.getEvent().getId())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        if (eventIds.size() < threshold) return List.of();
        List<Long> lastEventIds = eventIds.subList(eventIds.size() - threshold, eventIds.size());
        // Busca todos os membros que faltaram nos últimos N eventos
        List<Long> absents = attendanceRepository.findAllByStatusAndEventIds(AttendanceStatus.FALTOU, lastEventIds);
        // Conta quantas vezes cada membro aparece
        return absents.stream()
            .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() == threshold)
            .map(e -> e.getKey())
            .collect(Collectors.toList());
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
