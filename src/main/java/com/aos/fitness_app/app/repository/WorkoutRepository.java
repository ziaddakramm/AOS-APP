package com.aos.fitness_app.app.repository;


import com.aos.fitness_app.app.entity.WorkoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, Long> {

    public boolean existsByName(String name);

    @Query("SELECT COUNT(w) > 0 FROM WorkoutEntity w WHERE w.name = :name AND w.id != :excludeId")
    boolean existsByNameExcludingId(@Param("name") String name, @Param("excludeId") Long excludeId);
}