package com.example.Liderum.dto;

import com.example.Liderum.Enums.Classe;
import com.example.Liderum.Enums.GuildRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequestDTO {

    @NotBlank(message = "Nickname is required")
    @Size(min = 3, max = 30, message = "Nickname must be between 3 and 30 characters")
    private String nickname;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phone;

    @NotNull(message = "Guild role is required")
    private GuildRole guildRole;

    @NotBlank(message = "Rank is required")
    private String rank;

    @NotNull(message = "Class is required")
    private Classe classe;


    private Long teamId;
    private Long id;
}
