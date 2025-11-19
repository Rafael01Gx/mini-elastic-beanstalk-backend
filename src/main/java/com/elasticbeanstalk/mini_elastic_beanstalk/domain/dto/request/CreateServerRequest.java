package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request;

import lombok.Builder;

@Builder
public record CreateServerRequest(String name,String description) {
}
