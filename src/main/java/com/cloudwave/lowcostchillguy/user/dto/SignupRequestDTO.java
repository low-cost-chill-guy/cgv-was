package com.cloudwave.lowcostchillguy.user.dto;

public record SignupRequestDTO(
        String email,
        String password,
        String name
){}
