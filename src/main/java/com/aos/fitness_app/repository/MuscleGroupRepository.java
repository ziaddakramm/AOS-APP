package com.aos.fitness_app.repository;

import com.aos.fitness_app.entity.ExerciseTypeEntity;
import com.aos.fitness_app.entity.MuscleGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MuscleGroupRepository extends JpaRepository<MuscleGroupEntity, Long> {
}
