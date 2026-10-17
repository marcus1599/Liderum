package com.example.Liderum.dto;

import com.example.Liderum.exceptions.PasswordChangeException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequestDTO {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 128)
    private String newPassword;

    @JsonAnySetter
    public void rejectUnknownField(String ignoredName, Object ignoredValue) {
        throw new PasswordChangeException(PasswordChangeException.Code.INVALID_PASSWORD_REQUEST);
    }
}
