package com.example.Liderum.Services;

import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.dto.TeamResponseDTO;

import java.util.List;

public interface TeamService {
    TeamResponseDTO create(TeamRequestDTO requestDTO);
    List<TeamResponseDTO> findAll();
    TeamResponseDTO findById(Long id);
    void delete(Long id);
    void addMemberToTeam(Long teamId, Long memberId);
    void removeMemberFromTeam(Long teamId, Long memberId);
    void update(Long teamId);

}
