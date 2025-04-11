package com.example.Liderum.Services.Impl;

import com.example.Liderum.Entities.Member;
import com.example.Liderum.Entities.Team;
import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import com.example.Liderum.Repository.MemberRepository;
import com.example.Liderum.Repository.TeamRepository;
import com.example.Liderum.dto.MemberRequestDTO;
import com.example.Liderum.dto.MemberResponseDTO;

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

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    public void shouldCreateMemberSuccessfully() {
        MemberRequestDTO request = new MemberRequestDTO();
        request.setNickname("Marcus");
        request.setPhone("+5511999999999");
        request.setGuildRole(GuildRole.MARSHAL);
        request.setRank("Líder");
        request.setClasse(Classe.GUERREIRO);
        request.setTeamId(1L);

        Team team = Team.builder().id(1L).name("Guilda A").build();
        Member member = Member.builder()
                .id(1L)
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .guildRole(request.getGuildRole())
                .rank(request.getRank())
                .classe(request.getClasse())
                .team(team)
                .build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponseDTO response = memberService.create(request);

        assertNotNull(response);
        assertEquals("Marcus", response.getNickname());
        assertEquals("Líder", response.getRank());
        assertEquals("Guilda A", response.getTeamName());
    }

    @Test
    public void shouldFindAllMembers() {
        Team team = Team.builder().id(1L).name("Guilda A").build();
        Member member = Member.builder()
                .id(1L)
                .nickname("Marcus")
                .phone("123")
                .guildRole(GuildRole.MARSHAL)
                .rank("Líder")
                .classe(Classe.GUERREIRO)
                .team(team)
                .build();

        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<MemberResponseDTO> list = memberService.findAll();

        assertEquals(1, list.size());
        assertEquals("Marcus", list.get(0).getNickname());
    }

    @Test
    public void shouldFindMemberById() {
        Team team = Team.builder().id(1L).name("Guilda A").build();
        Member member = Member.builder()
                .id(1L)
                .nickname("Marcus")
                .phone("123")
                .guildRole(GuildRole.MARSHAL)
                .rank("Líder")
                .classe(Classe.GUERREIRO)
                .team(team)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberResponseDTO dto = memberService.findById(1L);

        assertNotNull(dto);
        assertEquals("Marcus", dto.getNickname());
    }

    @Test
    public void shouldDeleteMember() {
        Long id = 1L;

        doNothing().when(memberRepository).deleteById(id);

        memberService.delete(id);

        verify(memberRepository, times(1)).deleteById(id);
    }
}
