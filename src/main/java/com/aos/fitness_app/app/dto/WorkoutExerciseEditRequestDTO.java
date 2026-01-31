package com.aos.fitness_app.app.dto;


import com.aos.fitness_app.common.enums.OperationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutExerciseEditRequestDTO {
    private Long id;

    private Long exerciseId;

    @Min(value = 1, message = "Sets must be at least 1")
    private Integer sets;

    @Min(value = 1, message = "Reps must be at least 1")
    private Integer reps;

    @Min(value = 0, message = "Weight cannot be negative")
    private Double weight;

    @Min(value = 0, message = "Rest time cannot be negative")
    private Integer restTimeSeconds;

    private Integer order;

    @NotNull(message = "Operation type is required")
    private OperationType operationType;
}