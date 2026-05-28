package com.sciencelove.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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
public class CreateRequestRequest {

    @NotBlank(message = "Название темы обязательно")
    private String title;

    @NotBlank(message = "Описание темы обязательно")
    private String topic;

    @NotBlank(message = "Дисциплина обязательна")
    private String discipline;

    @Future(message = "Дедлайн должен быть в будущем")
    private LocalDate deadline;

    private BigDecimal budget;

    private String urgency;  // "LOW", "MEDIUM", "HIGH", "URGENT"

    private List<UUID> fieldIds;  // ID научных областей
}