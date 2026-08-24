package com.example.Liderum.dto;

import com.example.Liderum.Enums.GuildRole;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private GuildRole guildRole;
}
