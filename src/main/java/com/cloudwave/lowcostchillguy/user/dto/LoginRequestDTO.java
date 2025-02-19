package com.cloudwave.lowcostchillguy.user.dto;

import org.hibernate.validator.constraints.NotEmpty;

public record LoginRequestDTO(

    @NotEmpty(message = "이메일은 필수 값입니다.")
     String email,

    @NotEmpty(message = "비밀번호는 필수 값입니다.")
    String password
){}
