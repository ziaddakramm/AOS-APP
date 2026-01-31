package com.aos.fitness_app.app.service;

import com.aos.fitness_app.app.dto.*;
import com.aos.fitness_app.app.entity.ProgramEntity;
import com.aos.fitness_app.app.entity.ProgramWorkoutEntity;
import com.aos.fitness_app.app.entity.WorkoutEntity;
import com.aos.fitness_app.app.mapper.ProgramMapper;
import com.aos.fitness_app.app.repository.ProgramRepository;
import com.aos.fitness_app.app.repository.ProgramWorkoutRepository;
import com.aos.fitness_app.app.repository.WorkoutRepository;
import com.aos.fitness_app.common.enums.OperationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProgramService {

    private final ProgramRepository programRepository;
    private final WorkoutRepository workoutRepository;
    private final ProgramMapper programMapper;
    private final ProgramWorkoutRepository programWorkoutRepository;

    @Transactional
    public boolean createProgram(ProgramEditRequestDTO request) {
        log.info("Creating new program: {}", request.getName());

        // Check if program name already exists
        if (programRepository.existsByName(request.getName())) {
            throw new RuntimeException("Program with name '" + request.getName() + "' already exists");
        }

        // Create new program entity
        ProgramEntity program = new ProgramEntity();
        program.setName(request.getName());
        program.setDescription(request.getDescription());

        // Save program first to get ID
        ProgramEntity savedProgram = programRepository.save(program);

        // Add workouts if provided
        if (request.getWorkouts() != null && !request.getWorkouts().isEmpty()) {
            request.getWorkouts()
                    .stream()
                    .forEach(
                        programWorkoutEditDTO ->
                            {
                              addWorkoutToProgram(program,programWorkoutEditDTO);
                            }
                    );
        }
        savedProgram = programRepository.save(savedProgram);

        log.info("Created program '{}' with ID {} and {} workouts",
                savedProgram.getName(), savedProgram.getId(),
                savedProgram.getProgramWorkouts().size());

        return true;
    }
    @Transactional(readOnly = true)
    public Optional<ProgramEntity> getProgramById(Long id) {
        log.info("Fetching program with ID: {}", id);
        return programRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ProgramEntity> getAllPrograms() {
        log.info("Fetching all programs");
        return programRepository.findAll();
    }

    @Transactional
    public boolean deleteProgram(Long id) {
        log.debug("Deleting program with ID: {}", id);

        if (programRepository.existsById(id)) {
            programRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateProgram(Long programId, ProgramEditRequestDTO request) {
        log.info("Updating program {} with {} workouts", programId,
                request.getWorkouts() != null ? request.getWorkouts().size() : 0);

        //Get programs
        Optional<ProgramEntity> program = programRepository.findById(programId);

        if(program.isEmpty())
        {
            throw new RuntimeException("The requested program with id: " + programId + " doesn't exist");
        }

        // Update basic program info
        program.get().setName(request.getName());
        program.get().setDescription(request.getDescription());

        if (request.getWorkouts() != null) {
            processProgramWorkoutChanges(program.get(), request.getWorkouts());
        }

        programRepository.save(program.get());
        return true;
    }



    private void addWorkoutToProgram(ProgramEntity program, ProgramWorkoutEditRequestDTO workoutDto) {
            WorkoutEntity workout = workoutRepository.findById(workoutDto.getWorkoutId())
                    .orElseThrow(() -> new RuntimeException("Workout not found: " + workoutDto.getWorkoutId()));

            ProgramWorkoutEntity programWorkout = new ProgramWorkoutEntity();
            programWorkout.setProgram(program);
            programWorkout.setWorkout(workout);
            programWorkout.setOrder(workoutDto.getOrder());

            program.getProgramWorkouts().add(programWorkout);
    }

    public ProgramDTO mapProgramEntityToDTO(ProgramEntity programEntity)
    {
        return programMapper.mapToProgramDTO(programEntity);
    }

    public List<ProgramDTO> mapProgramEntitiesToDTOs(List<ProgramEntity> programEntities)
    {
        return programMapper.mapProgramEntityListToDTO(programEntities);
    }

    public void processProgramWorkoutChanges(ProgramEntity program, List<ProgramWorkoutEditRequestDTO> programEditRequestDTOs)
    {
         programEditRequestDTOs
                .forEach(requestDTO -> {
                    if (OperationType.UPDATE.equals(requestDTO.getOperationType())) {
                        updateProgramWorkout(program, requestDTO);
                    } else if (OperationType.CREATE.equals(requestDTO.getOperationType())) {
                        addWorkoutToProgram(program, requestDTO);
                    } else if (OperationType.DELETE.equals(requestDTO.getOperationType())) {
                        deleteProgramWorkout(program, requestDTO);
                    }
                });
    }

    public void updateProgramWorkout(ProgramEntity program, ProgramWorkoutEditRequestDTO programWorkoutEditDTO)
    {
        if(programWorkoutEditDTO.getId() == null)
        {
            throw new RuntimeException("Workout Exercise id was not provided for the update operation");
        }
        WorkoutEntity workout = workoutRepository.findById(programWorkoutEditDTO.getWorkoutId())
                .orElseThrow(() -> new RuntimeException("Workout not found: " + programWorkoutEditDTO.getWorkoutId()));

        // Find and update the ProgramWorkout in the collection
        program.getProgramWorkouts()
                .stream()
                .filter(programWorkout -> programWorkout.getId().equals(programWorkoutEditDTO.getId()))
                .findFirst()
                .ifPresentOrElse(
                        programWorkout -> {
                            programWorkout.setWorkout(workout);  // Update to new workout
                            programWorkout.setOrder(programWorkoutEditDTO.getOrder());  // Update order
                            log.info("Updated program workout {} for program {}", programWorkoutEditDTO.getId(), program.getId());
                        },
                        () -> {
                            throw new EntityNotFoundException("Program workout with id " + programWorkoutEditDTO.getId() + " not found in program " + program.getId());
                        }
                );
    }

    public void deleteProgramWorkout(ProgramEntity program, ProgramWorkoutEditRequestDTO programWorkoutEditDTO) {
        if(programWorkoutEditDTO.getId() == null)
        {
            throw new RuntimeException("Workout Exercise id was not provided for the delete operation");
        }

        workoutRepository.findById(programWorkoutEditDTO.getWorkoutId())
                .orElseThrow(() -> new RuntimeException("Workout not found: " + programWorkoutEditDTO.getWorkoutId()));

        boolean removed = program.getProgramWorkouts()
                .removeIf(programWorkout -> programWorkout.getId().equals(programWorkoutEditDTO.getId()));

        if (removed) {
            log.info("Deleted program workout {} from programs list {}", programWorkoutEditDTO.getId(), program.getId());
        } else {
            throw new EntityNotFoundException("Program workout with id " + programWorkoutEditDTO.getId() + " not found in program " + program.getId());
        }
    }
}