package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record ContainerConfig(
         String name,
         String image,
        List<String>environment,
         List<PortMapping> ports,
         List<VolumeMapping> volumes,
         String networkId,
         Map<String, String> labels,
         String command,
         String workingDir,
        Map<String, String>resources
) {
}
