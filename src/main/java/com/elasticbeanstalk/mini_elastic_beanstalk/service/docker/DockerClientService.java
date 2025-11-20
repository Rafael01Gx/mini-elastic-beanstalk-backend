package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.HealthStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.DockerOperationException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DockerClientService {

    @Autowired
    private DockerClient dockerClient;

    @Autowired
    public DockerClientService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Verificar se Docker daemon está acessível
     */
    public boolean isDockerAvailable() {
        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (Exception e) {
            log.error("Docker daemon não está acessível", e);
            return false;
        }
    }

    /**
     * Obter informações do Docker
     */
    public Info getDockerInfo() {
        try {
            return dockerClient.infoCmd().exec();
        } catch (Exception e) {
            log.error("Erro ao obter informações do Docker", e);
            throw new DockerOperationException("Erro ao obter informações do Docker"+ e.getMessage());
        }
    }

    /**
     * Obter versão do Docker
     */
    public Version getDockerVersion() {
        try {
            return dockerClient.versionCmd().exec();
        } catch (Exception e) {
            log.error("Erro ao obter versão do Docker", e);
            throw new DockerOperationException("Erro ao obter versão do Docker"+ e.getMessage());
        }
    }

    /**
     * Verificar saúde do Docker
     */
    public HealthStatus checkDockerHealth() {
        try {
            Info info = dockerClient.infoCmd().exec();
            Version version = dockerClient.versionCmd().exec();

            return HealthStatus.builder()
                    .available(true)
                    .version(version.getVersion())
                    .apiVersion(version.getApiVersion())
                    .containersRunning(info.getContainersRunning())
                    .containersStopped(info.getContainersStopped())
                    .images(info.getImages())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao verificar saúde do Docker", e);
            return HealthStatus.builder()
                    .available(false)
                    .error(e.getMessage())
                    .build();
        }
    }
}