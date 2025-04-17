package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Attendance;
import com.example.Liderum.Entities.Event;
import com.example.Liderum.Entities.Member;
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

    private AttendanceResponseDTO toDTO(Attendance attendance) {
        return AttendanceResponseDTO.builder()
                .id(attendance.getId())
                .eventName(attendance.getEvent().getName())
                .memberName(attendance.getMember().getNickname())
                .status(attendance.getStatus())
                .build();
    }
}
