package com.sciencelove.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse {

    private UUID id;
    private String title;
    private String topic;
    private String discipline;
    private LocalDate deadline;
    private BigDecimal budget;
    private String urgency;
    private String status;
    private List<FieldSummary> fields;
    private StudentSummary student;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldSummary {
        private UUID id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSummary {
        private UUID id;
        private String fullName;
        private String university;
    }
}