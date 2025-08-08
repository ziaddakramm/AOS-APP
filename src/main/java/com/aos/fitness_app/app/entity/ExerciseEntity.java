package com.aos.fitness_app.app.entity;

import com.aos.fitness_app.app.common.enums.ExerciseCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "exercise_name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ExerciseCategory category;

    // TODO: save video url
    // @Column(name = "media_file_references", columnDefinition = "text[]")
    // private String[] mediaFileReferences;

    // MANY exercises can use ONE equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", referencedColumnName = "id")
    private EquipmentEntity equipment;

    // MANY exercises can target ONE muscle group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muscle_group_id", referencedColumnName = "id")
    private MuscleGroupEntity muscleGroup;

    // MANY exercises can have ONE exercise type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_type_id", referencedColumnName = "id")
    private ExerciseTypeEntity exerciseType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}