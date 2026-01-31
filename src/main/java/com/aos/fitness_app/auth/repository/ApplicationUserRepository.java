package com.aos.fitness_app.auth.repository;


import com.aos.fitness_app.auth.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, String> {

  Optional<ApplicationUser> findByEmail(String email);

}
