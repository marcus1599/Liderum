package com.example.Liderum.Services;

import com.example.Liderum.dto.AttendanceRequestDTO;
import com.example.Liderum.dto.AttendanceResponseDTO;

import java.util.List;

public interface AttendanceService {
    AttendanceResponseDTO create(AttendanceRequestDTO dto);
    List<AttendanceResponseDTO> findAll();
    AttendanceResponseDTO findById(Long id);
    AttendanceResponseDTO update(Long id, AttendanceRequestDTO dto);
    void delete(Long id);
}
