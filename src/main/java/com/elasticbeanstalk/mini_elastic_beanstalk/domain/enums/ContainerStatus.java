package com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContainerStatus {
    PENDING("pending"),
    CREATED("created"),
    RUNNING("running"),
    STOPPED("stopped"),
    PAUSED("paused");

    private String dockerContainerStatus;

    ContainerStatus(String dockerContainerStatus) {
        this.dockerContainerStatus = dockerContainerStatus;
    }

    @JsonCreator
    public static ContainerStatus  fromDockerContainerStatus(String dockerContainerStatus) {
        for (ContainerStatus containerStatus : ContainerStatus.values()) {
            if (containerStatus.dockerContainerStatus.equals(dockerContainerStatus)) {
                return containerStatus;
            }
        }
        throw new IllegalArgumentException("Unknown container status " + dockerContainerStatus);
    };

    @JsonValue
    public String getDockerContainerStatus() {
        return dockerContainerStatus;
    }
}
