package com.aos.fitness_app.auth.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum Role {
    ADMIN, FRONT_DESK, COACH, USER
}