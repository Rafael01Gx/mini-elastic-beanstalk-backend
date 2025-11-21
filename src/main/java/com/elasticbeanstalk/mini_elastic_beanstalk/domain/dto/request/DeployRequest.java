package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record DeployRequest(
        @NotNull
        @NotEmpty
        String workspace,
        MultipartFile envFile,
        MultipartFile composeFile) {
}
