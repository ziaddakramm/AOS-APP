package com.aos.fitness_app.app.mapper;


import com.aos.fitness_app.app.dto.ProgramDTO;
import com.aos.fitness_app.app.dto.ProgramWorkoutDTO;
import com.aos.fitness_app.app.dto.WorkoutDTO;
import com.aos.fitness_app.app.dto.WorkoutExerciseDTO;
import com.aos.fitness_app.app.entity.ProgramEntity;
import com.aos.fitness_app.app.entity.ProgramWorkoutEntity;
import com.aos.fitness_app.app.entity.WorkoutEntity;
import com.aos.fitness_app.app.entity.WorkoutExerciseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkoutMapper {

    List<WorkoutDTO> mapWorkoutEntityListToDTO(List<WorkoutEntity> workoutEntities);


    @Mapping(target = "exercises", source = "workoutExercises")
    @Mapping(target = "exerciseCount", expression = "java(workoutEntity.getWorkoutExercises().size())")
    WorkoutDTO mapToWorkoutDTO(WorkoutEntity workoutEntity);

    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "exerciseName", source = "exercise.name")
    WorkoutExerciseDTO mapToWorkoutExerciseDTO(WorkoutExerciseEntity workoutExerciseEntity);

    @Named("mapProgramWorkoutsToDTO")
    default List<WorkoutExerciseDTO> mapWorkoutExercisesToDTO(List<WorkoutExerciseEntity> workoutExercises) {
        if (workoutExercises == null) return null;

        return workoutExercises.stream()
                .filter(workoutExercise -> workoutExercise != null && workoutExercise.getExercise() != null)
                .map(this::mapToWorkoutExerciseDTO)
                .toList();
    }

}