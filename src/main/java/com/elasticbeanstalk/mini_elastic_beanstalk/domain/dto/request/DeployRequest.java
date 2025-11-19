package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public record DeployRequest(String workspace, MultipartFile envFile, MultipartFile composeFile) {
}
