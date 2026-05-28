package com.sciencelove.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    private UUID mentorId;
    private String mentorName;
    private String degree;
    private String university;
    private BigDecimal rating;
    private BigDecimal hourlyRate;
    private Boolean isPremium;
    private Double matchScore;  // 0.0 - 1.0
    private String matchReason;  // "Совпадение по теме: биоинформатика"
}