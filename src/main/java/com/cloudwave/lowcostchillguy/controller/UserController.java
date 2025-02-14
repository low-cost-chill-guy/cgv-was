package com.cloudwave.lowcostchillguy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudwave.lowcostchillguy.service.UserService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
public class UserController {

    // 의존성 주입
    @Autowired
    private UserService userService;

    @GetMapping
    public List<String> getAllUsers() {
        return userService.getAllUsers();
    }

}
