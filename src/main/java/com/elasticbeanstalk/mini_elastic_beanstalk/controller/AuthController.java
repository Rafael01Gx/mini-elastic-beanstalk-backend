package com.elasticbeanstalk.mini_elastic_beanstalk.controller;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.LoginRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.AuthResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @RequestMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("logout")
    public String logout() {
        return "logout";
    }

    @GetMapping("me")
    public String me() {
        return "me";
    }
}
