package com.elasticbeanstalk.mini_elastic_beanstalk.service.auth;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.LoginRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.RegisterRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.AuthResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.User;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.UserRole;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
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
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));;

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    @Transactional
    public User createUser(RegisterRequest request) {

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

      return  userRepository.save(user);
    }
}