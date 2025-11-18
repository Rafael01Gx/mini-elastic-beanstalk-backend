package com.elasticbeanstalk.mini_elastic_beanstalk.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
