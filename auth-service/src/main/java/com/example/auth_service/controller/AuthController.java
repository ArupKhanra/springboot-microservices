package com.example.auth_service.controller;

import com.example.auth_service.entity.User;
import com.example.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.auth_service.dto.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(
            @RequestBody User user) {

        return userService.registerUser(
                user
        );
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @RequestBody RefreshTokenRequest request){

        return userService.refreshToken(request);
    }

    @PostMapping("/logout")
    public String logout(
            @RequestBody RefreshTokenRequest request){

        userService.logout(request);

        return "Logged out successfully";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/hello")
    public String hello(){
        return "Protected API";
    }


}