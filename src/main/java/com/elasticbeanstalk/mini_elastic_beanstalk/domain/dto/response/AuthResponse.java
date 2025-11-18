package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.UserRole;

public record AuthResponse(String token, String email, UserRole role) {

}
