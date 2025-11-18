package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull
        @NotBlank
        @Email
        String email,

        @NotNull
        @NotBlank
        String password) {
}
