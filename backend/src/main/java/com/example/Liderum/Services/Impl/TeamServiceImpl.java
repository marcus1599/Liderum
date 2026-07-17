package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Entities.User;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Repository.UserRepository;
import com.example.Liderum.Services.TeamService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.MemberResponseDTO;
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
    private final TenantService tenantService;

    @Override
    public TeamResponseDTO create(TeamRequestDTO dto) {
        Guild guild = tenantService.getCurrentGuild();
        User leader = resolveLeader(dto.getLeaderId(), guild.getId());

        Team team = Team.builder()
                .name(dto.getName())
                .leader(leader)
                .guild(guild)
                .build();

        return toResponseDTO(teamRepository.save(team));
    }

    @Override
    public List<TeamResponseDTO> findAll() {
        return teamRepository.findAllByGuildId(tenantService.getCurrentGuildId()).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponseDTO findById(Long id) {
        return toResponseDTO(findTeamInCurrentGuild(id));
    }

    @Override
    public void delete(Long id) {
        teamRepository.delete(findTeamInCurrentGuild(id));
    }

    @Override
    public void addMemberToTeam(Long teamId, Long memberId) {
        Long guildId = tenantService.getCurrentGuildId();
        Team team = findTeamInGuild(teamId, guildId);
        Member member = findMemberInGuild(memberId, guildId);

        member.setTeam(team);
        memberRepository.save(member);
    }

    @Override
    public void removeMemberFromTeam(Long teamId, Long memberId) {
        Long guildId = tenantService.getCurrentGuildId();
        Team team = findTeamInGuild(teamId, guildId);
        Member member = findMemberInGuild(memberId, guildId);

        if (member.getTeam() == null || !member.getTeam().getId().equals(team.getId())) {
            throw new RuntimeException("Member is not in the specified team");
        }

        member.setTeam(null);
        memberRepository.save(member);
    }

    @Override
    public void update(Long teamId) {
        Team team = findTeamInCurrentGuild(teamId);
        teamRepository.save(team);
    }

    private User resolveLeader(Long leaderId, Long guildId) {
        if (leaderId == null) {
            return tenantService.getCurrentUser();
        }

        return userRepository.findByIdAndGuildId(leaderId, guildId)
                .orElseThrow(() -> new EntityNotFoundException("Lider nao encontrado"));
    }

    private Team findTeamInCurrentGuild(Long teamId) {
        return findTeamInGuild(teamId, tenantService.getCurrentGuildId());
    }

    private Team findTeamInGuild(Long teamId, Long guildId) {
        return teamRepository.findByIdAndGuildId(teamId, guildId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe nao encontrada"));
    }

    private Member findMemberInGuild(Long memberId, Long guildId) {
        return memberRepository.findByIdAndGuildId(memberId, guildId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private TeamResponseDTO toResponseDTO(Team team) {
        List<MemberResponseDTO> members = team.getMembers() == null
                ? List.of()
                : team.getMembers().stream()
                        .map(member -> MemberResponseDTO.builder()
                                .id(member.getId())
                                .nickname(member.getNickname())
                                .classe(member.getClasse())
                                .rank(member.getRank())
                                .build())
                        .collect(Collectors.toList());

        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .leaderName(team.getLeader() != null ? team.getLeader().getUsername() : null)
                .members(members)
                .build();
    }
}
