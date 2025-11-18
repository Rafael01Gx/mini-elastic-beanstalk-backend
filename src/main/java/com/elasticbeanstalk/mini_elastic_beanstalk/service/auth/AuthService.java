package com.elasticbeanstalk.mini_elastic_beanstalk.service.auth;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.LoginRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.RegisterRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.AuthResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.User;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.UserRole;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${app.security.jwt.cookie-name}")
    private String cookieName;


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }
        User user = createUser(request);
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    public ResponseEntity<?> me(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(401).body("Cookie não encontrado");
        }
        String token = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                token = cookie.getValue();
                break;
            }
        }
        if (token == null) {
            return ResponseEntity.status(401).body("Cookie não encontrado");
        }
        User user = userRepository.findByEmail(jwtService.extractUsername(token)).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole()));
    }

    @Transactional
    public User createUser(RegisterRequest request) {

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

        return userRepository.save(user);
    }

    public void setAuthCookie(HttpServletResponse response, String token) {

        int maxAge = 86400; // 24h

        String cookie = String.format(
                "%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Lax",
                cookieName,
                token,
                maxAge
        );

        response.addHeader("Set-Cookie", cookie);
    }



    public void clearAuthCookie(HttpServletResponse response) {

        String cookie = String.format(
                "%s=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax",
                cookieName
        );
        response.addHeader("Set-Cookie", cookie);
    }

}