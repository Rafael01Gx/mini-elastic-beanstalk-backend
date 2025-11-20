package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ContainerConfig;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Container;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ExecResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.ContainerStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.DockerOperationException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ContainerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.util.DockerLabelUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DockerContainerService {

    @Autowired
    private DockerClient dockerClient;
    @Autowired
    private DockerLabelUtils labelUtils;
    @Autowired
    private ContainerRepository containerRepository;

    /**
     * Criar container com configuração personalizada
     */
    public String createContainer(String serverId, ContainerConfig config) {
        try {
            Map<String, String> labels = labelUtils.createServerLabels(serverId);
            labels.putAll(config.labels());

            CreateContainerCmd cmd = dockerClient.createContainerCmd(config.image())
                    .withName(config.name())
                    .withLabels(labels)
                    .withEnv(config.environment());

            // Configurar portas
            if (config.ports() != null && !config.ports().isEmpty()) {
                ExposedPort[] exposedPorts = config.ports().stream()
                        .map(p -> ExposedPort.tcp(p.containerPort()))
                        .toArray(ExposedPort[]::new);

                Ports portBindings = new Ports();
                config.ports().forEach(p -> {
                    portBindings.bind(
                            ExposedPort.tcp(p.containerPort()),
                            Ports.Binding.bindPort(p.hostPort())
                    );
                });

                cmd.withExposedPorts(exposedPorts).withHostConfig(
                        HostConfig.newHostConfig().withPortBindings(portBindings)
                );
            }

            // Configurar rede
            if (config.networkId() != null) {
                cmd.withHostConfig(
                        HostConfig.newHostConfig()
                                .withNetworkMode(config.networkId())
                );
            }

            // Configurar volumes
            if (config.volumes() != null && !config.volumes().isEmpty()) {
                List<Bind> binds = config.volumes().stream()
                        .map(v -> new Bind(v.hostPath(), new Volume(v.containerPath())))
                        .toList();

                cmd.withHostConfig(
                        HostConfig.newHostConfig().withBinds(binds)
                );
            }

            CreateContainerResponse response = cmd.exec();

            log.info("Container criado: {} para servidor: {}", response.getId(), serverId);

            return response.getId();

        } catch (Exception e) {
            log.error("Erro ao criar container", e);
            throw new DockerOperationException("Erro ao criar container"+e.getMessage());
        }
    }

    /**
     * Iniciar container
     */
    public void startContainer(String containerId) {
        try {
            dockerClient.startContainerCmd(containerId).exec();
            log.info("Container iniciado: {}", containerId);

            updateContainerStatus(containerId, ContainerStatus.RUNNING);

        } catch (Exception e) {
            log.error("Erro ao iniciar container: {}", containerId, e);
            throw new DockerOperationException("Erro ao iniciar container"+e.getMessage());
        }
    }

    /**
     * Parar container
     */
    public void stopContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId)
                    .withTimeout(10)
                    .exec();

            log.info("Container parado: {}", containerId);

            updateContainerStatus(containerId, ContainerStatus.STOPPED);

        } catch (Exception e) {
            log.error("Erro ao parar container: {}", containerId, e);
            throw new DockerOperationException("Erro ao parar container"+ e.getMessage());
        }
    }

    /**
     * Reiniciar container
     */
    public void restartContainer(String containerId) {
        try {
            dockerClient.restartContainerCmd(containerId)
                    .withtTimeout(10)
                    .exec();

            log.info("Container reiniciado: {}", containerId);

        } catch (Exception e) {
            log.error("Erro ao reiniciar container: {}", containerId, e);
            throw new DockerOperationException("Erro ao reiniciar container"+e.getMessage());
        }
    }

    /**
     * Remover container
     */
    public void removeContainer(String containerId, boolean force) {
        try {
            dockerClient.removeContainerCmd(containerId)
                    .withForce(force)
                    .withRemoveVolumes(true)
                    .exec();

            log.info("Container removido: {}", containerId);

            // Remover do banco
            containerRepository.deleteById(containerId);

        } catch (Exception e) {
            log.error("Erro ao remover container: {}", containerId, e);
            throw new DockerOperationException("Erro ao remover container"+e.getMessage());
        }
    }

    /**
     * Listar containers por servidor
     */
    public List<Container> listContainersByServer(String serverId) {
        try {
            Map<String, String> filters = labelUtils.createServerFilter(serverId);

            List<com.github.dockerjava.api.model.Container> containers =
                    dockerClient.listContainersCmd()
                            .withShowAll(true)
                            .withLabelFilter(filters)
                            .exec();

            return containers.stream()
                    .map(this::mapToContainer)
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao listar containers do servidor: {}", serverId, e);
            throw new DockerOperationException("Erro ao listar containers"+e.getMessage());
        }
    }

    /**
     * Inspecionar container
     */
    public InspectContainerResponse inspectContainer(String containerId) {
        try {
            return dockerClient.inspectContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("Erro ao inspecionar container: {}", containerId, e);
            throw new DockerOperationException("Erro ao inspecionar container"+ e.getMessage());
        }
    }

    /**
     * Obter estatísticas do container
     */
    public Statistics getContainerStats(String containerId) {
        try {
            StatsCallback callback = new StatsCallback();

            dockerClient.statsCmd(containerId)
                    .withNoStream(true)
                    .exec(callback);

            return callback.awaitResult();

        } catch (Exception e) {
            log.error("Erro ao obter estatísticas do container: {}", containerId, e);
            throw new DockerOperationException("Erro ao obter estatísticas: " + e.getMessage());
        }
    }

    public class StatsCallback extends ResultCallbackTemplate<StatsCallback, Statistics> {
        private Statistics statistics;

        @Override
        public void onNext(Statistics object) {
            this.statistics = object;
        }

        public Statistics awaitResult() {
            try {
                this.awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return statistics;
        }
    }


    /**
     * Executar comando em container
     */
    public ExecResult execCommand(String containerId, String... command) {
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient
                    .execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd(command)
                    .exec();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback(stdout, stderr))
                    .awaitCompletion();

            return ExecResult.builder()
                    .stdout(stdout.toString())
                    .stderr(stderr.toString())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao executar comando no container: {}", containerId, e);
            throw new DockerOperationException("Erro ao executar comando"+e.getMessage());
        }
    }

    /**
     * Copiar arquivo para container
     */
    public void copyFileToContainer(String containerId, Path sourcePath, String destPath) {
        try {
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withHostResource(sourcePath.toString())
                    .withRemotePath(destPath)
                    .exec();

            log.info("Arquivo copiado para container: {}", containerId);

        } catch (Exception e) {
            log.error("Erro ao copiar arquivo para container: {}", containerId, e);
            throw new DockerOperationException("Erro ao copiar arquivo"+e.getMessage());
        }
    }

    /**
     * Pausar container
     */
    public void pauseContainer(String containerId) {
        try {
            dockerClient.pauseContainerCmd(containerId).exec();
            log.info("Container pausado: {}", containerId);
            updateContainerStatus(containerId, ContainerStatus.PAUSED);
        } catch (Exception e) {
            log.error("Erro ao pausar container: {}", containerId, e);
            throw new DockerOperationException("Erro ao pausar container"+e.getMessage());
        }
    }

    /**
     * Despausar container
     */
    public void unpauseContainer(String containerId) {
        try {
            dockerClient.unpauseContainerCmd(containerId).exec();
            log.info("Container despausado: {}", containerId);
            updateContainerStatus(containerId, ContainerStatus.RUNNING);
        } catch (Exception e) {
            log.error("Erro ao despausar container: {}", containerId, e);
            throw new DockerOperationException("Erro ao despausar container"+e.getMessage());
        }
    }

    /**
     * Renomear container
     */
    public void renameContainer(String containerId, String newName) {
        try {
            dockerClient.renameContainerCmd(containerId)
                    .withName(newName)
                    .exec();

            log.info("Container renomeado: {} -> {}", containerId, newName);

        } catch (Exception e) {
            log.error("Erro ao renomear container: {}", containerId, e);
            throw new DockerOperationException("Erro ao renomear container"+e.getMessage());
        }
    }

    // Métodos auxiliares privados

    private Container mapToContainer(com.github.dockerjava.api.model.Container dockerContainer) {
        return Container.builder()
                .id(dockerContainer.getId())
                .name(dockerContainer.getNames()[0].replaceFirst("/", ""))
                .image(dockerContainer.getImage())
                .status(ContainerStatus.fromDockerContainerStatus(dockerContainer.getState()))
                .build();
    }

    private void updateContainerStatus(String containerId, ContainerStatus status) {
        containerRepository.findById(containerId).ifPresent(container -> {
            container.setStatus(status.name());
            container.setUpdatedAt(LocalDateTime.now());
            containerRepository.save(container);
        });
    }
}