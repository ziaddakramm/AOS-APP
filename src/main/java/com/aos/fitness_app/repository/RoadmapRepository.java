package com.aos.fitness_app.repository;


import com.aos.fitness_app.entity.RoadmapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapRepository extends JpaRepository<RoadmapEntity, Long> {
}
