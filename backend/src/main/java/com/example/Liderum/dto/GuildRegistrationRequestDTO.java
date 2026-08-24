package com.example.Liderum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuildRegistrationRequestDTO {
    @NotBlank @Size(max = 120)
    private String guildName;
    @NotBlank @Size(max = 120)
    private String serverName;
    @NotBlank @Size(min = 3, max = 80)
    private String username;
    @NotBlank @Email @Size(max = 160)
    private String email;
    @NotBlank @Size(min = 8, max = 128)
    private String password;
}
