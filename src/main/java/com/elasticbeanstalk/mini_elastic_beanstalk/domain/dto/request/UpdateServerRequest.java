package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request;

import java.time.LocalDateTime;

public record UpdateServerRequest(String name, String description, LocalDateTime updatedAt) {
}
