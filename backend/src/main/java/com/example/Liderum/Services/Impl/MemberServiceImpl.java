package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Services.MemberService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.MemberResponseDTO;
import com.example.Liderum.exceptions.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TenantService tenantService;

    @Override
    public MemberResponseDTO create(MemberRequestDTO dto) {
        Guild guild = tenantService.getCurrentGuild();
        Team team = resolveTeam(dto.getTeamId(), guild.getId());

        Member member = Member.builder()
                .nickname(dto.getNickname())
                .phone(dto.getPhone())
                .guildRole(dto.getGuildRole())
                .rank(dto.getRank())
                .classe(dto.getClasse())
                .team(team)
                .guild(guild)
                .build();

        return toDTO(memberRepository.save(member));
    }

    @Override
    public MemberResponseDTO update(Long id, MemberRequestDTO dto) {
        Long guildId = tenantService.getCurrentGuildId();
        Member existing = findMemberInCurrentGuild(id, guildId);

        existing.setNickname(dto.getNickname());
        existing.setPhone(dto.getPhone());
        existing.setClasse(dto.getClasse());
        existing.setGuildRole(dto.getGuildRole());
        existing.setRank(dto.getRank());
        existing.setTeam(resolveTeam(dto.getTeamId(), guildId));

        return toDTO(memberRepository.save(existing));
    }

    @Override
    public List<MemberResponseDTO> findAll() {
        return memberRepository.findAllByGuildId(tenantService.getCurrentGuildId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MemberResponseDTO findById(Long id) {
        return toDTO(findMemberInCurrentGuild(id, tenantService.getCurrentGuildId()));
    }

    @Override
    public void delete(Long id) {
        memberRepository.delete(findMemberInCurrentGuild(id, tenantService.getCurrentGuildId()));
    }

    private Member findMemberInCurrentGuild(Long id, Long guildId) {
        return memberRepository.findByIdAndGuildId(id, guildId)
                .orElseThrow(() -> new MemberNotFoundException("Member with ID " + id + " not found."));
    }

    private Team resolveTeam(Long teamId, Long guildId) {
        if (teamId == null) {
            return null;
        }

        return teamRepository.findByIdAndGuildId(teamId, guildId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
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
