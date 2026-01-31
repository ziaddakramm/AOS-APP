package com.aos.fitness_app.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDTO {
    private Long id;
    private String name;
    private String workoutType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkoutExerciseDTO> exercises;
    private int exerciseCount;
}