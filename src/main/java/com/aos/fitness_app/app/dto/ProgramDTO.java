package com.aos.fitness_app.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramDTO {
    private Long id;
    private String name;
    private String description;
    private List<ProgramWorkoutDTO> workouts;
}
