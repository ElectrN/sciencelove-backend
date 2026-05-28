package com.sciencelove.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorResponse {

    private UUID id;
    private String degree;
    private String university;
    private String bio;
    private BigDecimal rating;
    private Integer totalConsultations;
    private BigDecimal hourlyRate;
    private Boolean isPremium;
    private List<FieldSummary> interests;
    private List<String> expertise;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldSummary {
        private UUID id;
        private String name;
        private String slug;
    }
}