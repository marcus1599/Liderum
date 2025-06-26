package com.example.Liderum.dto;

import com.example.Liderum.Enums.AttendanceStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AttendanceResponseDTO {
    private Long id;
    private Long memberId;
    private Long eventId;
    private String memberName;
    private String eventName;
    private AttendanceStatus status;
}
