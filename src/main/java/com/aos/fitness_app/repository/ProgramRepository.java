package com.aos.fitness_app.repository;


import com.aos.fitness_app.entity.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramEntity, Long> {



}
