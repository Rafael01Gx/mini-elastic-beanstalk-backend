package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LogMessage (
         String containerId,
         LocalDateTime timestamp,
         String message,
         String level // INFO, WARN, ERROR, DEBUG
){

}