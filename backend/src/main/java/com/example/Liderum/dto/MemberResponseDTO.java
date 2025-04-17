package com.example.Liderum.dto;

import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponseDTO {
    private Long id;
    private String nickname;
    private String phone;
    private GuildRole guildRole;
    private String rank;
    private String teamName; // Podemos buscar o nome do time para exibir
    private Classe classe;
}
