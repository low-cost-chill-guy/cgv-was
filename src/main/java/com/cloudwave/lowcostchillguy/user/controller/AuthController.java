package com.cloudwave.lowcostchillguy.user.controller;

import com.cloudwave.lowcostchillguy.user.dto.LoginRequestDTO;
import com.cloudwave.lowcostchillguy.user.dto.SignupRequestDTO;
import com.cloudwave.lowcostchillguy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    //로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.login(loginRequestDTO);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO signupRequestDTO) {
        return userService.signup(signupRequestDTO);
    }
}
