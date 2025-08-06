package com.aos.fitness_app.controller;


import com.aos.fitness_app.repository.ExerciseRepository;
import com.aos.fitness_app.repository.WorkoutRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity testDb() {

        // ExerciseRepository.save(exerciseEntity);

        log.info(exerciseRepository.findAll().toString());

        return ResponseEntity.ok("test");
    }
}