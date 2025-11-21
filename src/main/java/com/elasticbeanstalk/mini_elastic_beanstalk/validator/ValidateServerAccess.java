package com.elasticbeanstalk.mini_elastic_beanstalk.validator;

import com.elasticbeanstalk.mini_elastic_beanstalk.exception.ResourceNotFoundException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.UnauthorizedException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ValidateServerAccess {
    @Autowired
    private ServerRepository serverRepository;

    public void validate(String serverId, Long userId) {
        Boolean asUser = serverRepository.existsByIdAndUserId(serverId,userId);
        if (!asUser) {
            throw new UnauthorizedException("Acesso negado, servidor não existe ou não pertence ao usuário");
        }
    }
}
