package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ErrorResponse(Integer status, Map<String, String> errors, String message, LocalDateTime timestamp) {
}
