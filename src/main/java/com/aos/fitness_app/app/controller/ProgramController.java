package com.aos.fitness_app.app.controller;

import com.aos.fitness_app.app.dto.ProgramDTO;
import com.aos.fitness_app.app.dto.ProgramEditRequestDTO;
import com.aos.fitness_app.app.entity.ProgramEntity;
import com.aos.fitness_app.app.service.ProgramService;
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
@RequestMapping("/programs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Program operations")
public class ProgramController {

    private final ProgramService programService;


    @GetMapping("")
    @Operation(summary = "Get program cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ProgramDTO>> getPrograms() {
        log.info("Getting program cards");

        List<ProgramEntity> programEntities = programService.getAllPrograms();

        if(programEntities.isEmpty())
        {
            throw new RuntimeException("No programs exist at the moment");
        }

        return ResponseEntity.ok(
                programService.mapProgramEntitiesToDTOs(programEntities)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get program for editing")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProgramDTO> getProgram(@PathVariable Long id) {
        log.info("Getting program {} for editing", id);

       Optional<ProgramEntity> programEntity = programService.getProgramById(id);

       if(programEntity.isEmpty())
       {
           throw new RuntimeException("The request program with id: " + id + "doesn't exist");
       }

       return ResponseEntity.ok(
               programService.mapProgramEntityToDTO(programEntity.get()
               )
       );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update program")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody ProgramEditRequestDTO request) {
        log.info("Updating program {} with {} workouts", id,
                request.getWorkouts() != null ? request.getWorkouts().size() : 0);
            Boolean isSuccess = programService.updateProgram(id, request);
            return ResponseEntity.ok(isSuccess);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete program")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> deleteProgram(
            @PathVariable Long id) {
        log.info("Deleting program with id: {} ", id);
        Boolean isSuccess = programService.deleteProgram(id);
        return ResponseEntity.ok(isSuccess);
    }

    @PostMapping("")
    @Operation(summary = "Create program")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> createProgram(@RequestBody ProgramEditRequestDTO request) {
        log.info("Creating a new program");
        Boolean isSuccess = programService.createProgram(request);
        return ResponseEntity.ok(isSuccess);
    }


}