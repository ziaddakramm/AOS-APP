package com.aos.fitness_app.app.repository;

import com.aos.fitness_app.app.entity.WorkoutExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExerciseEntity, Long> {
}
