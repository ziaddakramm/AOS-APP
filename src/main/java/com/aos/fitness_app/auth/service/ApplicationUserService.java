package com.aos.fitness_app.auth.service;

import com.aos.fitness_app.auth.entity.ApplicationUser;
import com.aos.fitness_app.auth.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationUserService {

    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;
    public void updatePassword(ApplicationUser user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        applicationUserRepository.save(user);
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        Optional<ApplicationUser> user = applicationUserRepository.findByEmail(email);
        if(user.isPresent()){
            updatePassword(user.get(), newPassword);
        }else{
            throw new RuntimeException("The use with email: " + email + " doesn't exist!");
        }
    }


}
