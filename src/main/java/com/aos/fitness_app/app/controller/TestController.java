package com.aos.fitness_app.app.controller;


import com.aos.fitness_app.app.repository.ExerciseRepository;
import com.aos.fitness_app.app.repository.WorkoutRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@Slf4j
@Tag(name = "Fitness App Test Controller", description = "Test API")
@RestController
@CrossOrigin
@RequestMapping(value = "/test")
@RequiredArgsConstructor
public class TestController {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final EasyRandom easyRandom = new EasyRandom();

    @GetMapping("/db")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity testDb() {

        // ExerciseRepository.save(exerciseEntity);

        log.info(exerciseRepository.findAll().toString());

        return ResponseEntity.ok("test");
    }
}