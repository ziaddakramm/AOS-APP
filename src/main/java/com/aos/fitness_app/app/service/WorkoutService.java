package com.aos.fitness_app.app.service;

import com.aos.fitness_app.app.dto.WorkoutDTO;
import com.aos.fitness_app.app.dto.WorkoutEditRequestDTO;
import com.aos.fitness_app.app.dto.WorkoutExerciseEditRequestDTO;
import com.aos.fitness_app.app.entity.ExerciseEntity;
import com.aos.fitness_app.app.entity.WorkoutEntity;
import com.aos.fitness_app.app.entity.WorkoutExerciseEntity;
import com.aos.fitness_app.app.mapper.WorkoutMapper;
import com.aos.fitness_app.app.repository.ExerciseRepository;
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
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutMapper workoutMapper;

    @Transactional(readOnly = true)
    public List<WorkoutDTO> getAllWorkouts() {
        log.info("Fetching all active workouts");
        List<WorkoutEntity> workouts = workoutRepository.findAll();
        if(workouts.isEmpty())
        {
            throw new RuntimeException("No workouts exist at the moment");
        }
        return workoutMapper.mapWorkoutEntityListToDTO(workouts);
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutDTO> getWorkoutById(Long id) {
        log.info("Fetching workout with ID: {}", id);
        Optional<WorkoutEntity> workout = workoutRepository.findById(id);
        if(!workout.isPresent())
        {
            throw new RuntimeException("The requested workout with id: " + id + "doesn't exist");
        }
        return workout.map(workoutMapper::mapToWorkoutDTO);
    }

    @Transactional
    public boolean createWorkout(WorkoutEditRequestDTO request) {
        if (!OperationType.CREATE.equals(request.getOperationType())) {
            throw new IllegalArgumentException("Create endpoint requires CREATE operation type, got: " + request.getOperationType());
        }

        log.info("Creating new workout: {}", request.getName());

        // Check if workout name already exists
        if (workoutRepository.existsByName(request.getName())) {
            throw new RuntimeException("Workout with name '" + request.getName() + "' already exists");
        }

        // Create new workout entity
        WorkoutEntity workout = new WorkoutEntity();
        workout.setName(request.getName());
        workout.setWorkoutType(request.getWorkoutType());

        // Save workout first to get ID
        WorkoutEntity savedWorkout = workoutRepository.save(workout);

        // Add exercises if provided
        if (request.getExercises() != null && !request.getExercises().isEmpty()) {
            request.getExercises().forEach(exerciseDTO -> addExerciseToWorkout(savedWorkout, exerciseDTO));
        }

        // Save again to persist exercises
        workoutRepository.save(savedWorkout);

        log.info("Created workout '{}' with ID {} and {} exercises",
                savedWorkout.getName(), savedWorkout.getId(),
                savedWorkout.getWorkoutExercises().size());

        return true;
    }

    @Transactional
    public boolean updateWorkout(Long workoutId, WorkoutEditRequestDTO request) {
        if (!OperationType.UPDATE.equals(request.getOperationType())) {
            throw new IllegalArgumentException("Update endpoint requires UPDATE operation type, got: " + request.getOperationType());
        }

        log.info("Updating workout {} with {} exercises", workoutId,
                request.getExercises() != null ? request.getExercises().size() : 0);

        // Get workout with exercises
        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found with id: " + workoutId));

        // Check name uniqueness if name is being changed
        if (!workout.getName().equals(request.getName()) &&
                workoutRepository.existsByNameExcludingId(request.getName(), workoutId)) {
            throw new RuntimeException("Workout with name '" + request.getName() + "' already exists");
        }

        // Update basic workout info
        updateBasicWorkoutInfo(workout, request);

        // Process exercise changes if provided
        if (request.getExercises() != null) {
            processWorkoutExerciseChanges(workout, request.getExercises());
        }

        workoutRepository.save(workout);
        return true;
    }

    @Transactional
    public boolean deleteWorkout(Long id) {
        log.info("Deleting workout with ID: {}", id);

        WorkoutEntity workout = workoutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found with id: " + id));

        workoutRepository.deleteById(id);

        log.info("Deleted workout with ID: {}", id);
        return true;
    }

    // Private helper methods (same pattern as ProgramService)

    private void updateBasicWorkoutInfo(WorkoutEntity workout, WorkoutEditRequestDTO request) {
        if (request.getName() != null) {
            workout.setName(request.getName());
        }
        if (request.getWorkoutType() != null) {
            workout.setWorkoutType(request.getWorkoutType());
        }
    }

    private void processWorkoutExerciseChanges(WorkoutEntity workout, List<WorkoutExerciseEditRequestDTO> exerciseChanges) {
        // Separate operations by type (same pattern as ProgramService)
        List<WorkoutExerciseEditRequestDTO> deletes = exerciseChanges.stream()
                .filter(dto -> OperationType.DELETE.equals(dto.getOperationType()))
                .toList();

        List<WorkoutExerciseEditRequestDTO> updates = exerciseChanges.stream()
                .filter(dto -> OperationType.UPDATE.equals(dto.getOperationType()))
                .toList();

        List<WorkoutExerciseEditRequestDTO> creates = exerciseChanges.stream()
                .filter(dto -> OperationType.CREATE.equals(dto.getOperationType()))
                .toList();

        // Process in order: deletes, updates, creates
        deletes.forEach(dto -> deleteWorkoutExercise(workout, dto));
        updates.forEach(dto -> updateWorkoutExercise(workout, dto));
        creates.forEach(dto -> addExerciseToWorkout(workout, dto));

        // Reorder exercises after all operations
        reorderWorkoutExercises(workout);
    }

    private void addExerciseToWorkout(WorkoutEntity workout, WorkoutExerciseEditRequestDTO exerciseDto) {
        ExerciseEntity exercise = exerciseRepository.findById(exerciseDto.getExerciseId())
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + exerciseDto.getExerciseId()));

        WorkoutExerciseEntity workoutExercise = new WorkoutExerciseEntity();
        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setSets(exerciseDto.getSets());
        workoutExercise.setReps(exerciseDto.getReps());
        workoutExercise.setWeight(exerciseDto.getWeight());
        workoutExercise.setRestTimeSeconds(exerciseDto.getRestTimeSeconds());
        workoutExercise.setOrder(exerciseDto.getOrder() != null ? exerciseDto.getOrder() : getNextOrder(workout));

        workout.getWorkoutExercises().add(workoutExercise);
        log.debug("Added exercise {} to workout {}", exerciseDto.getExerciseId(), workout.getId());
    }

    private void updateWorkoutExercise(WorkoutEntity workout, WorkoutExerciseEditRequestDTO exerciseDTO) {
        ExerciseEntity exercise = exerciseRepository.findById(exerciseDTO.getExerciseId())
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + exerciseDTO.getExerciseId()));

        workout.getWorkoutExercises()
                .stream()
                .filter(we -> we.getId().equals(exerciseDTO.getId()))
                .findFirst()
                .ifPresentOrElse(
                        workoutExercise -> {
                            workoutExercise.setExercise(exercise);
                            workoutExercise.setSets(exerciseDTO.getSets());
                            workoutExercise.setReps(exerciseDTO.getReps());
                            workoutExercise.setWeight(exerciseDTO.getWeight());
                            workoutExercise.setRestTimeSeconds(exerciseDTO.getRestTimeSeconds());
                            workoutExercise.setOrder(exerciseDTO.getOrder());
                            log.debug("Updated workout exercise {} for workout {}", exerciseDTO.getId(), workout.getId());
                        },
                        () -> {
                            throw new EntityNotFoundException("Workout exercise with id " + exerciseDTO.getId() + " not found in workout " + workout.getId());
                        }
                );
    }

    private void deleteWorkoutExercise(WorkoutEntity workout, WorkoutExerciseEditRequestDTO exerciseDTO) {
        // Find the exercise to delete
        WorkoutExerciseEntity toDelete = workout.getWorkoutExercises()
                .stream()
                .filter(workoutExercise -> workoutExercise.getId().equals(exerciseDTO.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Workout exercise with id " + exerciseDTO.getId() + " not found"));

        int deletedOrder = toDelete.getOrder();

        // Remove the exercise
        workout.getWorkoutExercises().remove(toDelete);

        // Adjust orders of remaining exercises that come after the deleted one
        workout.getWorkoutExercises()
                .stream()
                .filter(we -> we.getOrder() > deletedOrder)
                .forEach(we -> we.setOrder(we.getOrder() - 1));

        log.info("Deleted workout exercise at position {} and reordered remaining exercises", deletedOrder);
    }

    private void reorderWorkoutExercises(WorkoutEntity workout) {
        List<WorkoutExerciseEntity> exercises = workout.getWorkoutExercises()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getOrder() != null ? a.getOrder() : 0,
                        b.getOrder() != null ? b.getOrder() : 0))
                .toList();

        // Reassign orders sequentially starting from 1
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setOrder(i + 1);
        }

        log.debug("Reordered {} workout exercises", exercises.size());
    }

    private int getNextOrder(WorkoutEntity workout) {
        return workout.getWorkoutExercises()
                .stream()
                .mapToInt(we -> we.getOrder() != null ? we.getOrder() : 0)
                .max()
                .orElse(0) + 1;
    }
}