package ru.sciencelove.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentProfileRequest {

    private String university;

    private String faculty;

    @Min(value = 1, message = "Курс должен быть от 1 до 6")
    @Max(value = 6, message = "Курс должен быть от 1 до 6")
    private Integer course;

    private String degreeType;  // "BACHELOR", "MASTER", "SPECIALIST"

    private List<String> interests;

    private List<String> goals;
}