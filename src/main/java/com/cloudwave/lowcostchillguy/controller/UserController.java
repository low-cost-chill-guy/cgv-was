package com.cloudwave.lowcostchillguy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cloudwave.lowcostchillguy.service.UserService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    // 테스트 getmapping
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

}
