package com.aos.fitness_app.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramEditRequestDTO {

    private Long id;

    @NotBlank(message = "Program name is required")
    @Size(max = 255, message = "Program name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Valid
    private List<ProgramWorkoutEditRequestDTO> workouts;
}