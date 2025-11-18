package com.elasticbeanstalk.mini_elastic_beanstalk.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
