package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.TeamService;
import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.dto.TeamResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    public TeamResponseDTO create(TeamRequestDTO dto) {
        User leader = userRepository.findById(dto.getLeaderId())
                .orElseThrow(() -> new EntityNotFoundException("Líder não encontrado"));

        Team team = Team.builder()
                .name(dto.getName())
                .leader(leader)
                .build();

        teamRepository.save(team);

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .leaderName(leader.getUsername())
                .build();
    }

    @Override
    public List<TeamResponseDTO> findAll() {
        return teamRepository.findAll().stream().map(team ->
                TeamResponseDTO.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .leaderName(team.getLeader().getUsername())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public TeamResponseDTO findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipe não encontrada"));

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .leaderName(team.getLeader().getUsername())
                .build();
    }

    @Override
    public void delete(Long id) {
        teamRepository.deleteById(id);
    }
    @Override
public void addMemberToTeam(Long teamId, Long memberId) {
    Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));

    member.setTeam(team);
    memberRepository.save(member);
}

@Override
public void removeMemberFromTeam(Long teamId, Long memberId) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));

    if (member.getTeam() == null || !member.getTeam().getId().equals(teamId)) {
        throw new RuntimeException("Member is not in the specified team");
    }

    member.setTeam(null);
    memberRepository.save(member);
}

}
