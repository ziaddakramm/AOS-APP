package com.aos.fitness_app.auth.repository;


import com.aos.fitness_app.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByEmailAndOtpAndUsedFalse(String email, String otp);

    Optional<PasswordResetOtp> findByEmailAndUsedFalse(String email);

    void deleteByEmail(String email);

    @Query("SELECT p FROM PasswordResetOtp p WHERE p.expiryTime < :now")
    List<PasswordResetOtp> findExpiredOtps(@Param("now") LocalDateTime now);

}
