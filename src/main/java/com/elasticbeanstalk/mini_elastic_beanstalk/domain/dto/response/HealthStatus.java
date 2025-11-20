package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

@Builder
public record HealthStatus(

         Boolean available,
         String version,
         String apiVersion,
         Integer containersRunning,
         Integer containersStopped,
         Integer images,
         String error

) {
}
