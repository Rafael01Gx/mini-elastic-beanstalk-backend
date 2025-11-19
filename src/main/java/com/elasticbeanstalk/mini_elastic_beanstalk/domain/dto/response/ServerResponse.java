package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.ServerStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ServerResponse(String id, String name, String description, ServerStatus status, String networkId, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
