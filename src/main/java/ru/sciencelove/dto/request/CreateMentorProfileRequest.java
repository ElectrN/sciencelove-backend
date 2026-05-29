package ru.sciencelove.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMentorProfileRequest {

    private String degree;

    private String university;

    private String department;

    private String bio;

    private List<String> expertise;

    @DecimalMin(value = "0.0", inclusive = false, message = "Ставка должна быть больше 0")
    private BigDecimal hourlyRate;

    private Integer minSessionMinutes;

    private List<Integer> availableDays;

    private LocalTime availableHoursStart;

    private LocalTime availableHoursEnd;

    private List<UUID> interestFieldIds;
}