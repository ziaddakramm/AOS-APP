package com.aos.fitness_app.entity;


import com.aos.fitness_app.common.enums.WorkoutDifficultyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

//    TODO: see if this was necessary
//    @Enumerated(EnumType.STRING)
//    @Column(name = "difficulty_level")
//    private WorkoutDifficultyLevel difficultyLevel;

    @Column(name = "workout_type")
    private String workoutType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkoutExerciseEntity> workoutExercises = new ArrayList<>();
}