package com.example.auth_service.controller;

import com.example.auth_service.entity.User;
import com.example.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/users/{id}/roles")
    public User updateRoles(
            @PathVariable Long id,
            @RequestBody List<String> roles) {

        return userService.updateRoles(id, roles);
    }
}
