package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DeployResult(Boolean success,Long deployId ,String errorMessage, String output, List<String> containers,List<String> names) {
}
