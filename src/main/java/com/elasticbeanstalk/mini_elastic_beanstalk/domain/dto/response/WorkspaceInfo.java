package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WorkspaceInfo (
     String name,
     String path,
     Long size,
     Integer fileCount,
     LocalDateTime createdAt,
     LocalDateTime modifiedAt
)
{}