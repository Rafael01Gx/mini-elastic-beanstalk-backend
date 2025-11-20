package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

@Builder
public record VolumeMapping(
         String hostPath,
         String containerPath,
         String mode
) {
}
