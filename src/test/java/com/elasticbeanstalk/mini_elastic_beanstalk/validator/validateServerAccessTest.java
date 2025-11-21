package com.elasticbeanstalk.mini_elastic_beanstalk.validator;

import com.elasticbeanstalk.mini_elastic_beanstalk.exception.UnauthorizedException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ServerRepository;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateServerAccessTest {

    @InjectMocks
    private ValidateServerAccess validateServerAccess;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private Server server;

    @Test
    @DisplayName("Deve retornar void se o ServerId pertencer ao UserId")
    void cenario1(){
        Long userId = 1L;
        String serverId = "serverId";

        BDDMockito.given(serverRepository.existsByIdAndUserId(serverId, userId)).willReturn(true);

        Assertions.assertDoesNotThrow(() -> validateServerAccess.validate(serverId, userId));

        BDDMockito.verify(serverRepository, BDDMockito.times(1)).existsByIdAndUserId(serverId, userId);
    }

    @Test
    @DisplayName("Deve retornar UnauthorizedException se o ServerId não pertencer ao UserId")
    void cenario2(){
        Long userId = 1L;
        String serverId = "serverId";

        BDDMockito.given(serverRepository.existsByIdAndUserId(serverId, userId)).willReturn(false);

        Assertions.assertThrows(UnauthorizedException.class,() -> validateServerAccess.validate(serverId, userId));

        BDDMockito.verify(serverRepository, BDDMockito.times(1)).existsByIdAndUserId(serverId, userId);
    }

}