package com.example.Liderum.dto;

import com.example.Liderum.Enums.GuildRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class UserCreateRequestDTO {
    @NotBlank
    @Size(min = 3, max = 80)
    private String username;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @JsonIgnore
    private String password;

    @NotNull
    private GuildRole role;
}
