package com.cloudwave.lowcostchillguy.user.controller;

import com.cloudwave.lowcostchillguy.user.dto.LoginRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    //로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loginRequestDTO);
    }

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
}
