package com.elasticbeanstalk.mini_elastic_beanstalk.exception;

public class DockerOperationException extends RuntimeException {
    public DockerOperationException(String message) {
        super(message);
    }
}
