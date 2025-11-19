package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileInfo (
     String name,
     String path,
     Long size,
     Boolean isDirectory,
     LocalDateTime createdAt,
     LocalDateTime modifiedAt
) {}