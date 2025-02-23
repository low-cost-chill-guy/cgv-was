package com.cloudwave.lowcostchillguy.user.dto;

import org.hibernate.validator.constraints.NotEmpty;

public record LoginRequestDTO(
     String email,

    String password
){}
