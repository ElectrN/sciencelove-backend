package ru.sciencelove.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestRequest {

    private String title;

    private String topic;

    private String discipline;

    private LocalDate deadline;

    @DecimalMin(value = "0.0", inclusive = false, message = "Бюджет должен быть больше 0")
    private BigDecimal budget;

    private String urgency;

    private List<UUID> fieldIds;
}