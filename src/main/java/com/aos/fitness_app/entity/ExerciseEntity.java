package com.aos.fitness_app.entity;

import com.aos.fitness_app.common.enums.ExerciseCategory;
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

    @Column(name = "exercise_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseCategory type;

    // TODO: save video url
    // @Column(name = "media_file_references", columnDefinition = "text[]")
    // private String[] mediaFileReferences;

    //Todo: Relations to Equipment, Exercise Type and Muscle Groups

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