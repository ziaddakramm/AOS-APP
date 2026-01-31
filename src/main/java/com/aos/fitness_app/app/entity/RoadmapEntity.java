package com.aos.fitness_app.app.entity;

import com.aos.fitness_app.common.enums.RoadmapCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roadmap_name", nullable = false)
    private String name;

    @Column(name = "duration_weeks") // Total duration of the roadmap
    private Integer durationWeeks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "category")
    private Enum<RoadmapCategory> category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roadmap_level",
            joinColumns = @JoinColumn(name = "roadmap_id"),
            inverseJoinColumns = @JoinColumn(name = "level_id")
    )
    private List<LevelEntity> levels = new ArrayList<>();

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