package com.elasticbeanstalk.mini_elastic_beanstalk.service.server;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.CreateServerRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.request.UpdateServerRequest;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ServerResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.AuditLog;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Server;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.User;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.ServerStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.ResourceNotFoundException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.UnauthorizedException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.AuditLogRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ServerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.UserRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.auth.AuthService;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.docker.DockerNetworkService;
import com.elasticbeanstalk.mini_elastic_beanstalk.util.FileSystemUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ServerService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private DockerNetworkService dockerNetworkService;
    @Autowired
    private FileSystemUtils fileSystemUtils;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Value("${app.storage.base-path}")
    private String basePath;

    @Transactional
    public ServerResponse createServer(CreateServerRequest dto, HttpServletRequest request) {
        Long userId = authService.getUserGetFromCookie(request).getId();
        User user = userRepository.getReferenceById(userId);
        String serverId = UUID.randomUUID().toString();

        try {
            // Criar diretório do servidor
            Path serverPath = Paths.get(basePath, serverId);
            Files.createDirectories(serverPath);
            Files.createDirectories(serverPath.resolve("workspaces"));
            Files.createDirectories(serverPath.resolve("logs"));

            // Criar rede Docker
            String networkId = dockerNetworkService.createServerNetwork(serverId);

            // Criar registro no banco
            Server server = Server.builder()
                    .id(serverId)
                    .user(user)
                    .name(dto.name())
                    .description(dto.description())
                    .status(ServerStatus.ACTIVE)
                    .networkId(networkId)
                    .build();

            serverRepository.save(server);

            // Auditoria
            logAudit(userId, serverId, "SERVER_CREATED",
                    Map.of("name", dto.name()));

            log.info("Servidor criado: {} para usuário: {}", serverId, userId);

            return mapToResponse(server);

        } catch (Exception e) {
            log.error("Erro ao criar servidor", e);
            cleanupFailedServer(serverId);
            throw new BusinessException("Erro ao criar servidor: " + e.getMessage());
        }
    }

    public List<ServerResponse> listUserServers(HttpServletRequest request) {
        Long userId = authService.getUserGetFromCookie(request).getId();
        return serverRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ServerResponse getServer(String serverId, HttpServletRequest request) {
        Long userId = authService.getUserGetFromCookie(request).getId();
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        if (!server.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Acesso negado");
        }

        return mapToResponse(server);
    }

    @Transactional
    public ServerResponse updateServer(String serverId, UpdateServerRequest dto, HttpServletRequest request) {
        Long userId = authService.getUserGetFromCookie(request).getId();
        Server server = getServerEntity(serverId, userId);

        server.setName(dto.name());
        server.setDescription(dto.description());
        server.setUpdatedAt(LocalDateTime.now());

        serverRepository.save(server);

        logAudit(userId, serverId, "SERVER_UPDATED",
                Map.of("name", dto.name()));

        return mapToResponse(server);
    }

    @Transactional
    public void deleteServer(String serverId, HttpServletRequest request) {
        Long userId = authService.getUserGetFromCookie(request).getId();
        Server server = getServerEntity(serverId, userId);

        try {
            // Remover containers, imagens, volumes
           // dockerCleanupService.cleanupServerResources(serverId);

            // Remover rede
            if (server.getNetworkId() != null) {
                dockerNetworkService.removeNetwork(server.getNetworkId());
            }

            // Remover diretório
            Path serverPath = Paths.get(basePath, serverId);
            fileSystemUtils.deleteDirectory(serverPath);

            // Remover do banco
            serverRepository.delete(server);

            logAudit(userId, serverId, "SERVER_DELETED", null);

            log.info("Servidor removido completamente: {}", serverId);

        } catch (Exception e) {
            log.error("Erro ao remover servidor", e);
            throw new BusinessException("Erro ao remover servidor: " + e.getMessage());
        }
    }

    private Server getServerEntity(String serverId, Long userId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        if (!server.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Acesso negado");
        }

        return server;
    }

    private void cleanupFailedServer(String serverId) {
        // Implementar limpeza em caso de falha
    }

    private ServerResponse mapToResponse(Server server) {
        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .description(server.getDescription())
                .status(server.getStatus())
                .networkId(server.getNetworkId())
                .createdAt(server.getCreatedAt())
                .updatedAt(server.getUpdatedAt())
                .build();
    }

    private void logAudit(Long userId, String serverId, String action, Map<String, Object> details) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .serverId(serverId)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }
}