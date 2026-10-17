package com.example.Liderum.dto;

import com.example.Liderum.Enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequestDTO {
    @NotNull
    private UserStatus status;
}
