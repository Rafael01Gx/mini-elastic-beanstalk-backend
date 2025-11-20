package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

public record PortMapping(
         Integer hostPort,
         Integer containerPort,
         String protocol
) {
}
