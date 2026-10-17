package com.example.Liderum.Services;

import com.example.Liderum.dto.PasswordChangeRequestDTO;

public interface PasswordChangeService {
    void changeCurrentUserPassword(PasswordChangeRequestDTO request);
}
