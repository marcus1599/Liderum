package com.example.Liderum.Services;

import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.MemberResponseDTO;

import java.util.List;

public interface MemberService {
    MemberResponseDTO create(MemberRequestDTO dto);
    List<MemberResponseDTO> findAll();
    MemberResponseDTO findById(Long id);
    void delete(Long id);
    MemberResponseDTO update(Long id, MemberRequestDTO dto);
}
