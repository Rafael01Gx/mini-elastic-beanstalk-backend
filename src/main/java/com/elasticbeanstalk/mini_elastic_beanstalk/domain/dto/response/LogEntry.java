package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LogEntry(
        LocalDateTime timestamp,
         String message,
         String level,
         String source
) {
}
