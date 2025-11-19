package com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public String getFirstError() {
        if (errors != null && !errors.isEmpty()) {
            return errors.get(0);
        }
        return null;
    }
}