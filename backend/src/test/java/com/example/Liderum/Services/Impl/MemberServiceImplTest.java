package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Guild;
import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.Tenancy.TenantService;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.MemberResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Guild guild;

    @BeforeEach
    public void setup() {
        guild = Guild.builder().id(1L).name("Guilda Teste").build();

        lenient().when(tenantService.getCurrentGuild()).thenReturn(guild);
        lenient().when(tenantService.getCurrentGuildId()).thenReturn(1L);
    }

    @Test
    public void shouldCreateMemberSuccessfully() {
        MemberRequestDTO request = new MemberRequestDTO();
        request.setNickname("Marcus");
        request.setPhone("+5511999999999");
        request.setGuildRole(GuildRole.MARECHAL);
        request.setRank("Líder");
        request.setClasse(Classe.GUERREIRO);
        request.setTeamId(1L);

        Team team = Team.builder().id(1L).name("Guilda A").guild(guild).build();
        Member member = Member.builder()
                .id(1L)
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .guildRole(request.getGuildRole())
                .rank(request.getRank())
                .classe(request.getClasse())
                .team(team)
                .guild(guild)
                .build();

        when(teamRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(team));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponseDTO response = memberService.create(request);

        assertNotNull(response);
        assertEquals("Marcus", response.getNickname());
        assertEquals("Líder", response.getRank());
        assertEquals("Guilda A", response.getTeamName());
    }

    @Test
    public void shouldFindAllMembers() {
        Team team = Team.builder().id(1L).name("Guilda A").guild(guild).build();
        Member member = Member.builder()
                .id(1L)
                .nickname("Marcus")
                .phone("123")
                .guildRole(GuildRole.MARECHAL)
                .rank("Líder")
                .classe(Classe.GUERREIRO)
                .team(team)
                .guild(guild)
                .build();

        when(memberRepository.findAllByGuildId(1L)).thenReturn(List.of(member));

        List<MemberResponseDTO> list = memberService.findAll();

        assertEquals(1, list.size());
        assertEquals("Marcus", list.get(0).getNickname());
    }

    @Test
    public void shouldFindMemberById() {
        Team team = Team.builder().id(1L).name("Guilda A").guild(guild).build();
        Member member = Member.builder()
                .id(1L)
                .nickname("Marcus")
                .phone("123")
                .guildRole(GuildRole.MARECHAL)
                .rank("Líder")
                .classe(Classe.GUERREIRO)
                .team(team)
                .guild(guild)
                .build();

        when(memberRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(member));

        MemberResponseDTO dto = memberService.findById(1L);

        assertNotNull(dto);
        assertEquals("Marcus", dto.getNickname());
    }

    @Test
    public void shouldDeleteMember() {
        Long id = 1L;
        Team team = Team.builder().id(1L).name("Guilda A").guild(guild).build();
        Member member = Member.builder()
                .id(1L)
                .nickname("Marcus")
                .phone("123")
                .guildRole(GuildRole.MARECHAL)
                .rank("Líder")
                .classe(Classe.GUERREIRO)
                .team(team)
                .guild(guild)
                .build();

        when(memberRepository.findByIdAndGuildId(1L, 1L)).thenReturn(Optional.of(member));
        doNothing().when(memberRepository).delete(member);

        memberService.delete(id);

        verify(memberRepository, times(1)).delete(member);
    }
}
