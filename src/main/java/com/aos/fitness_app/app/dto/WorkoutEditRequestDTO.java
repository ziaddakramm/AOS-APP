package com.aos.fitness_app.app.dto;

import com.aos.fitness_app.common.enums.OperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutEditRequestDTO {
    private Long id;

    @NotBlank(message = "Workout name is required")
    @Size(max = 200, message = "Workout name must not exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Workout type must not exceed 100 characters")
    private String workoutType;

    private Boolean isActive = true;

    @Valid
    private List<WorkoutExerciseEditRequestDTO> exercises;

    private OperationType operationType;
}
