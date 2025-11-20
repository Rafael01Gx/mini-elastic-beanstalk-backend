package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DockerImageService {

    @Autowired
    private DockerClient dockerClient;

    /**
     * Pull de imagem de forma assíncrona
     */
    @Async("dockerExecutor")
    public CompletableFuture<Void> pullImageAsync(String imageName) {
        try {
            log.info("Iniciando pull da imagem: {}", imageName);

            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();

            log.info("Pull concluído: {}", imageName);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Erro ao fazer pull da imagem: {}", imageName, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}