package com.example.Liderum.dto;

import com.example.Liderum.Enums.GuildRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequestDTO {
    @NotNull
    private GuildRole role;
}
