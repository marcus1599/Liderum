package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Services.TeamService;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.MemberResponseDTO;
import com.example.Liderum.dto.TeamLeaderCandidateDTO;
import com.example.Liderum.dto.TeamRequestDTO;
import com.example.Liderum.dto.TeamResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TenantService tenantService;

    @Override
    @Transactional
    public TeamResponseDTO create(TeamRequestDTO dto) {
        Guild guild = tenantService.getCurrentGuild();
        Team team = Team.builder().name(dto.getName()).guild(guild).build();
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
    @Transactional
    public void delete(Long id) {
        Long guildId = tenantService.getCurrentGuildId();
        memberRepository.findAllByTeamAndGuildForUpdate(id, guildId);
        Team team = teamRepository.findByIdAndGuildIdForUpdate(id, guildId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe nao encontrada"));
        if (team.getLeader() != null) {
            team.setLeader(null);
            teamRepository.saveAndFlush(team);
        }
        teamRepository.delete(team);
    }

    @Override
    @Transactional
    public void addMemberToTeam(Long teamId, Long memberId) {
        Long guildId = tenantService.getCurrentGuildId();
        Member member = findMemberForUpdate(memberId, guildId);
        Long currentTeamId = member.getTeam() == null ? null : member.getTeam().getId();
        Map<Long, Team> lockedTeams = lockTeamsInOrder(guildId, currentTeamId, teamId);
        Team target = requireLockedTeam(lockedTeams, teamId);

        if (currentTeamId != null && !currentTeamId.equals(teamId)) {
            clearLeaderIfAssigned(lockedTeams.get(currentTeamId), member);
        }
        member.setTeam(target);
        memberRepository.saveAndFlush(member);
    }

    @Override
    @Transactional
    public void removeMemberFromTeam(Long teamId, Long memberId) {
        Long guildId = tenantService.getCurrentGuildId();
        Member member = findMemberForUpdate(memberId, guildId);
        Long currentTeamId = member.getTeam() == null ? null : member.getTeam().getId();
        Map<Long, Team> lockedTeams = lockTeamsInOrder(guildId, currentTeamId, teamId);
        Team team = requireLockedTeam(lockedTeams, teamId);

        if (!teamId.equals(currentTeamId)) {
            throw new EntityNotFoundException("Member nao pertence a equipe informada");
        }
        clearLeaderIfAssigned(team, member);
        member.setTeam(null);
        memberRepository.saveAndFlush(member);
    }

    @Override
    @Transactional
    public TeamResponseDTO update(Long teamId, TeamRequestDTO request) {
        Team team = findTeamInCurrentGuild(teamId);
        if (request != null && request.getName() != null) {
            team.setName(request.getName());
        }
        return toResponseDTO(teamRepository.save(team));
    }

    @Override
    public List<TeamLeaderCandidateDTO> findLeaderCandidates(Long teamId) {
        Long guildId = tenantService.getCurrentGuildId();
        findTeamInGuild(teamId, guildId);
        return memberRepository.findAllByTeamAndGuild(teamId, guildId).stream()
                .map(member -> new TeamLeaderCandidateDTO(member.getId(), member.getNickname()))
                .toList();
    }

    @Override
    @Transactional
    public TeamResponseDTO reassignLeader(Long teamId, Long leaderMemberId) {
        Long guildId = tenantService.getCurrentGuildId();
        Member leader = findMemberForUpdate(leaderMemberId, guildId);
        Team team = teamRepository.findByIdAndGuildIdForUpdate(teamId, guildId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe nao encontrada"));
        if (leader.getTeam() == null || !teamId.equals(leader.getTeam().getId())) {
            throw new EntityNotFoundException("Lider deve pertencer a equipe informada");
        }
        team.setLeader(leader);
        return toResponseDTO(teamRepository.save(team));
    }

    private Member findMemberForUpdate(Long memberId, Long guildId) {
        return memberRepository.findByIdAndGuildIdForUpdate(memberId, guildId)
                .orElseThrow(() -> new EntityNotFoundException("Member nao encontrado"));
    }

    private Map<Long, Team> lockTeamsInOrder(Long guildId, Long... ids) {
        List<Long> sortedIds = java.util.Arrays.stream(ids)
                .filter(id -> id != null)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        return lockTeamsInOrder(guildId, sortedIds);
    }

    private Map<Long, Team> lockTeamsInOrder(Long guildId, Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Team> teams = teamRepository.findAllByIdsAndGuildIdForUpdate(ids, guildId);
        Map<Long, Team> result = new LinkedHashMap<>();
        teams.forEach(team -> result.put(team.getId(), team));
        return result;
    }

    private Team requireLockedTeam(Map<Long, Team> teams, Long teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            throw new EntityNotFoundException("Equipe nao encontrada");
        }
        return team;
    }

    private void clearLeaderIfAssigned(Team team, Member member) {
        if (team != null && team.getLeader() != null && team.getLeader().getId().equals(member.getId())) {
            team.setLeader(null);
            teamRepository.saveAndFlush(team);
        }
    }

    private Team findTeamInCurrentGuild(Long teamId) {
        return findTeamInGuild(teamId, tenantService.getCurrentGuildId());
    }

    private Team findTeamInGuild(Long teamId, Long guildId) {
        return teamRepository.findByIdAndGuildId(teamId, guildId)
                .orElseThrow(() -> new EntityNotFoundException("Equipe nao encontrada"));
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
                .leaderMemberId(team.getLeader() != null ? team.getLeader().getId() : null)
                .leaderNickname(team.getLeader() != null ? team.getLeader().getNickname() : null)
                .members(members)
                .build();
    }
}
