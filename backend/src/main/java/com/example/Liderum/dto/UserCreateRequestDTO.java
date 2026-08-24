package com.example.Liderum.dto;

import com.example.Liderum.Enums.GuildRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequestDTO {
    @NotBlank
    @Size(min = 3, max = 80)
    private String username;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @NotNull
    private GuildRole role;
}
