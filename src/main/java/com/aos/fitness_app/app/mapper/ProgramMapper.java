package com.aos.fitness_app.app.mapper;

import com.aos.fitness_app.app.dto.ProgramDTO;
import com.aos.fitness_app.app.dto.ProgramWorkoutDTO;
import com.aos.fitness_app.app.entity.ProgramEntity;
import com.aos.fitness_app.app.entity.ProgramWorkoutEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProgramMapper {

    List<ProgramDTO> mapProgramEntityListToDTO(List<ProgramEntity> programEntities);

    @Mapping(target = "workouts", source = "programWorkouts", qualifiedByName = "mapProgramWorkoutsToDTO")
    ProgramDTO mapToProgramDTO(ProgramEntity programEntity);

    @Named("mapProgramWorkoutsToDTO")
    default List<ProgramWorkoutDTO> mapProgramWorkoutsToDTO(List<ProgramWorkoutEntity> programWorkouts) {
        if (programWorkouts == null) return null;

        return programWorkouts.stream()
                .filter(programWorkout -> programWorkout != null && programWorkout.getWorkout() != null)
                .map(this::mapToProgramWorkoutDTO)
                .toList();
    }

    default ProgramWorkoutDTO mapToProgramWorkoutDTO(ProgramWorkoutEntity pw) {
        if (pw == null || pw.getWorkout() == null) return null;

        return ProgramWorkoutDTO.builder()
                .id(pw.getWorkout().getId())
                .name(pw.getWorkout().getName())
                .orderInProgram(pw.getOrder() != null ? pw.getOrder() : 0)
                .build();
    }
}