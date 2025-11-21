package com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.DeployRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ValidationResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Container;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Deploy;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Server;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.DeployStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.ResourceNotFoundException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.UnauthorizedException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ContainerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.DeployRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ServerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.auth.AuthService;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.docker.DockerComposeService;
import com.elasticbeanstalk.mini_elastic_beanstalk.validator.ValidateServerAccess;
import jakarta.servlet.http.HttpServletRequest;
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
    private DeployExecutionService deployExecutionService;
    @Autowired
    private AuthService authService;
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
    @Autowired
    private ValidateServerAccess validateServerAccess;


    @Value("${app.storage.base-path}")
    private String basePath;

    @Transactional
    public DeployResponse deploy(String serverId, DeployRequest dto, HttpServletRequest req) {
        Long userId = authService.getUserGetFromCookie(req).getId();

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        if (!server.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Acesso negado");
        }

        String workspace = dto.workspace();
        Path workspacePath = workspaceService.createWorkspace(serverId, workspace);

        try {
            Path composePath = workspacePath.resolve("docker-compose.yml");
            Path envPath = workspacePath.resolve(".env");

            fileUploadService.saveFile(dto.composeFile(), composePath);
            if (dto.envFile() != null) {
                fileUploadService.saveFile(dto.envFile(), envPath);
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

            CompletableFuture.runAsync(() -> deployExecutionService.executeDeployment(deploy.getId()));

            return mapToResponse(deploy);

        } catch (Exception e) {
            log.error("Erro ao preparar deploy", e);
            throw new BusinessException("Erro no deploy: " + e.getMessage());
        }
    }


    public List<DeployResponse> listDeploys(String serverId, HttpServletRequest req) {

        Long userId = authService.getUserGetFromCookie(req).getId();
        validateServerAccess.validate(serverId, userId);

        return deployRepository.findByServerId(serverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void removeDeploy(String serverId, Long deployId, HttpServletRequest req) {
        Long userId = authService.getUserGetFromCookie(req).getId();
        validateServerAccess.validate(serverId, userId);

        Deploy deploy = deployRepository.findById(deployId)
                .orElseThrow(() -> new ResourceNotFoundException("Deploy não encontrado"));

        String projectName = "app-" + serverId;
        dockerComposeService.removeCompose(serverId, projectName);

        deployRepository.delete(deploy);

        log.info("Deploy removido: {}", deployId);
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