package com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.DeployRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ValidationResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Deploy;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Server;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.DeployStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.ResourceNotFoundException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.UnauthorizedException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.DeployRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ServerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.docker.DockerComposeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DeployService {

    @Autowired
    private DeployRepository deployRepository;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private DockerComposeService dockerComposeService;
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private WorkspaceService workspaceService;

    @Value("${app.storage.base-path}")
    private String basePath;

    @Transactional
    public DeployResponse deploy(String serverId, DeployRequest request, Long userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        if (!server.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Acesso negado");
        }

        String workspace = request.workspace();
        Path workspacePath = workspaceService.createWorkspace(serverId, workspace);

        try {
            Path composePath = workspacePath.resolve("docker-compose.yml");
            Path envPath = workspacePath.resolve(".env");

            fileUploadService.saveFile(request.composeFile(), composePath);
            if (request.envFile() != null) {
                fileUploadService.saveFile(request.envFile(), envPath);
            }

            ValidationResult validation = dockerComposeService.validateCompose(composePath);
            if (!validation.isValid()) {
                throw new BusinessException("Compose inválido: " + validation.getErrors());
            }
            serverRepository.findById(serverId);
            dockerComposeService.injectLabels(composePath, serverId);

            Deploy deploy = Deploy.builder()
                    .server(server)
                    .workspace(workspace)
                    .composePath(composePath.toString())
                    .envPath(envPath.toString())
                    .status(DeployStatus.PENDING)
                    .build();

            deployRepository.save(deploy);

            CompletableFuture.runAsync(() -> executeDeployment(deploy, composePath));

            return mapToResponse(deploy);

        } catch (Exception e) {
            log.error("Erro ao preparar deploy", e);
            throw new BusinessException("Erro no deploy: " + e.getMessage());
        }
    }

    private void executeDeployment(Deploy deploy, Path composePath) {
        try {
            deploy.setStatus(DeployStatus.DEPLOYING);
            deployRepository.save(deploy);

            DeployResult result = dockerComposeService.deployCompose(
                    deploy.getServer().getId(),
                    composePath
            );

            if (result.success()) {
                deploy.setStatus(DeployStatus.SUCCESS);
                registerContainers(deploy.getServer().getId(), result.containers());
                log.info("Deploy concluído com sucesso: {}", deploy.getId());
            } else {
                deploy.setStatus(DeployStatus.FAILED);
                deploy.setErrorMessage(result.errorMessage());
                log.error("Deploy falhou: {}", result.errorMessage());
            }

        } catch (Exception e) {
            deploy.setStatus(DeployStatus.FAILED);
            deploy.setErrorMessage(e.getMessage());
            log.error("Erro ao executar deploy", e);
        } finally {
            deploy.setUpdatedAt(LocalDateTime.now());
            deployRepository.save(deploy);
        }
    }

    private void registerContainers(String serverId, List<String> containerIds) {
        // Implementar registro dos containers no banco
    }

    public List<DeployResponse> listDeploys(String serverId, Long userId) {
        validateServerAccess(serverId, userId);

        return deployRepository.findByServerId(serverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void removeDeploy(String serverId, Long deployId, Long userId) {
        validateServerAccess(serverId, userId);

        Deploy deploy = deployRepository.findById(deployId)
                .orElseThrow(() -> new ResourceNotFoundException("Deploy não encontrado"));

        String projectName = "app-" + serverId;
        dockerComposeService.removeCompose(serverId, projectName);

        deployRepository.delete(deploy);

        log.info("Deploy removido: {}", deployId);
    }

    private void validateServerAccess(String serverId, Long userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        if (!server.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Acesso negado");
        }
    }

    private DeployResponse mapToResponse(Deploy deploy) {
        return DeployResponse.builder()
                .id(deploy.getId())
                .serverId(deploy.getServer().getId())
                .workspace(deploy.getWorkspace())
                .status(deploy.getStatus())
                .errorMessage(deploy.getErrorMessage())
                .createdAt(deploy.getCreatedAt())
                .updatedAt(deploy.getUpdatedAt())
                .build();
    }
}