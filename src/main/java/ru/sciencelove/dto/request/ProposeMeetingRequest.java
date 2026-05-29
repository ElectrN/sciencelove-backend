package ru.sciencelove.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposeMeetingRequest {

    @Future(message = "Время встречи должно быть в будущем")
    private LocalDateTime startTime;

    @Min(value = 15, message = "Минимальная длительность — 15 минут")
    @Max(value = 180, message = "Максимальная длительность — 180 минут")
    private Integer durationMinutes;

    @NotBlank(message = "Тип локации обязателен")
    private String locationType;  // "ONLINE", "OFFLINE", "HYBRID"

    private String locationDetails;  // Ссылка на звонок или адрес
}