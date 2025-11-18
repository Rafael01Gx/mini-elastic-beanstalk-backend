package com.elasticbeanstalk.mini_elastic_beanstalk.controller;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.LoginRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.RegisterRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.AuthResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);
        authService.setAuthCookie(response, authResponse.token());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        authService.clearAuthCookie(response);
        return ResponseEntity.ok("Logout realizado com sucesso");
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        return authService.me(request);
    }


}
