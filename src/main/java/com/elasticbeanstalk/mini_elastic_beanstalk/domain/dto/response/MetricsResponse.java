package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MetricsResponse(

        String serverId,
        Integer totalContainers,
         Integer runningContainers,
        Double avgCpuUsage,
        Long totalMemoryUsageMB,
        Long totalMemoryLimitMB,
        LocalDateTime timestamp
) {
}
