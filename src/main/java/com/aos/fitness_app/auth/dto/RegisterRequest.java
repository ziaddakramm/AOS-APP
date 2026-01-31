package com.aos.fitness_app.auth.dto;

import com.aos.fitness_app.auth.constraints.ValidPassword;
import com.aos.fitness_app.common.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @Email
  private String email;

  @ValidPassword
  private String password;

  private Role role;
}
