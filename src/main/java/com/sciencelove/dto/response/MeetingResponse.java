package com.sciencelove.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {

    private UUID id;
    private LocalDateTime startTime;
    private Integer durationMinutes;
    private String locationType;
    private String locationDetails;
    private String status;  // "PENDING", "CONFIRMED", etc.
    private String proposedByName;
    private LocalDateTime createdAt;
}