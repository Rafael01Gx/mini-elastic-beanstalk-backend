package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

@Builder
public record HealthResponse(
        String serverId,
        Integer healthyContainers,
        Integer unhealthyContainers,
        Integer noHealthcheck,
        String overallStatus
) {
}
