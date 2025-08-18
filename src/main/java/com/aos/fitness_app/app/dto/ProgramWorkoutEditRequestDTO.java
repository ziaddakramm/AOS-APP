package com.aos.fitness_app.app.dto;


import com.aos.fitness_app.common.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class ProgramWorkoutEditRequestDTO {
        private Long id;
        @NotNull(message = "Workout ID is required")
        private Long workoutId;
        private Integer order;
        private OperationType operationType;
    }