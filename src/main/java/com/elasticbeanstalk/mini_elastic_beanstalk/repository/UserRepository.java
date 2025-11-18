package com.elasticbeanstalk.mini_elastic_beanstalk.repository;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(@NotNull @NotBlank @Email String email);

    Optional<User> findByEmail(String email);

}
