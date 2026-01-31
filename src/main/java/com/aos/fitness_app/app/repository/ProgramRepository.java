package com.aos.fitness_app.app.repository;


import com.aos.fitness_app.app.entity.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramEntity, Long> {

    Optional<ProgramEntity> findById(@Param("id") Long id);

    boolean existsByName(String name);
}