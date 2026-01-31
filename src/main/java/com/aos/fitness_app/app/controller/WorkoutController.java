package com.aos.fitness_app.app.controller;

import com.aos.fitness_app.app.dto.WorkoutDTO;
import com.aos.fitness_app.app.dto.WorkoutEditRequestDTO;
import com.aos.fitness_app.app.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workouts operations")
public class WorkoutController {

    private final WorkoutService workoutService;


    @GetMapping("")
    @Operation(summary = "Get workout cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkoutDTO>> getWorkouts() {
        log.info("Getting workout cards");
        List<WorkoutDTO> workoutDTOS = workoutService.getAllWorkouts();
        return ResponseEntity.ok(
                workoutDTOS
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workout for editing")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WorkoutDTO> getWorkout(@PathVariable Long id) {
        log.info("Getting workout {}", id);
        return ResponseEntity.ok(
                workoutService.getWorkoutById(id).get()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> updateWorkout(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutEditRequestDTO request) {
        log.info("Updating workout {} with {} workouts", id,
                request.getExercises() != null ? request.getExercises().size() : 0);
        Boolean isSuccess = workoutService.updateWorkout(id, request);
        return ResponseEntity.ok(isSuccess);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> deleteWorkout(
            @PathVariable Long id) {
        log.info("Deleting workout with id: {} ", id);
        Boolean isSuccess = workoutService.deleteWorkout(id);
        return ResponseEntity.ok(isSuccess);
    }

    @PostMapping("")
    @Operation(summary = "Create workout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> createWorkout(@RequestBody WorkoutEditRequestDTO request) {
        log.info("Creating a new workout");
        Boolean isSuccess = workoutService.createWorkout(request);
        return ResponseEntity.ok(isSuccess);
    }




}
