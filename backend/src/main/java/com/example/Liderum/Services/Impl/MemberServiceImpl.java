package com.example.Liderum.Services.Impl;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Services.MemberService;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.Liderum.exceptions.MemberNotFoundException;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    @Override
public MemberResponseDTO create(MemberRequestDTO dto) {
    Team team = null;
    if (dto.getTeamId() != null) {
        team = teamRepository.findById(dto.getTeamId())
            .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    Member member = Member.builder()
            .nickname(dto.getNickname())
            .phone(dto.getPhone())
            .guildRole(dto.getGuildRole())
            .rank(dto.getRank())
            .classe(dto.getClasse())
            .team(team)
            .build();

    Member saved = memberRepository.save(member);
    return toDTO(saved);
}
public MemberResponseDTO update(Long id, MemberRequestDTO dto) {
    Member existing = memberRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Membro não encontrado com ID: " + id));

  
    existing.setNickname(dto.getNickname());
    existing.setPhone(dto.getPhone());
    existing.setClasse(dto.getClasse());
    existing.setGuildRole(dto.getGuildRole());
    existing.setRank(dto.getRank());

   
  // existing.setTeam(teamRepository.findById(dto.getTeamId()).orElseThrow(...));

    Member updated = memberRepository.save(existing);
    return toDTO(updated);
}



    @Override
    public List<MemberResponseDTO> findAll() {
        return memberRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
public MemberResponseDTO findById(Long id) {
    Member member = memberRepository.findById(id)
            .orElseThrow(() -> new MemberNotFoundException("Member with ID " + id + " not found."));
  
    return toDTO(member);
}

    @Override
    public void delete(Long id) {
        memberRepository.deleteById(id);
    }

    private MemberResponseDTO toDTO(Member member) {
        return MemberResponseDTO.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .rank(member.getRank())
                .guildRole(member.getGuildRole())
                .classe(member.getClasse())
                .teamName(member.getTeam() != null ? member.getTeam().getName() : null)
                .build();
    }
}
