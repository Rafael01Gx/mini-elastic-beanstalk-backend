package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.DeployStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DeployResponse(Long id, String serverId, String workspace, DeployStatus status, String errorMessage, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
