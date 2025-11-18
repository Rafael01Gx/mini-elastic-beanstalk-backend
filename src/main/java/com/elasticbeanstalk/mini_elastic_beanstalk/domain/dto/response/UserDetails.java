package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.User;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.UserRole;

public record UserDetails(String name , String email, UserRole role) {

    public UserDetails(User user) {
        this(user.getName(), user.getEmail(), user.getRole());
    }
}
