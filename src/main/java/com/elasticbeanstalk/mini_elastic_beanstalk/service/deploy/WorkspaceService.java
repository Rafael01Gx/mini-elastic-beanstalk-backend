package com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.FileInfo;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.WorkspaceInfo;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.ResourceNotFoundException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ServerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.util.FileSystemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WorkspaceService {
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private FileSystemUtils fileSystemUtils;

    @Value("${app.storage.base-path}")
    private String basePath;

    /**
     * Criar workspace para servidor
     */
    public Path createWorkspace(String serverId, String workspaceName) {
        try {
            // Validar workspace name
            validateWorkspaceName(workspaceName);

            // Verificar se servidor existe
            serverRepository.findById(serverId)
                    .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

            // Criar path do workspace
            Path workspacePath = Paths.get(basePath, serverId, "workspaces", workspaceName);

            // Criar diretórios
            fileSystemUtils.ensureDirectoryExists(workspacePath);

            log.info("Workspace criado: {} para servidor: {}", workspaceName, serverId);

            return workspacePath;

        } catch (Exception e) {
            log.error("Erro ao criar workspace", e);
            throw new BusinessException("Erro ao criar workspace: " + e.getMessage());
        }
    }

    /**
     * Listar workspaces do servidor
     */
    public List<WorkspaceInfo> listWorkspaces(String serverId) {
        try {
            Path workspacesPath = Paths.get(basePath, serverId, "workspaces");

            if (!Files.exists(workspacesPath)) {
                return List.of();
            }

            List<WorkspaceInfo> workspaces = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(workspacesPath)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        workspaces.add(createWorkspaceInfo(path));
                    }
                }
            }

            return workspaces;

        } catch (Exception e) {
            log.error("Erro ao listar workspaces do servidor: {}", serverId, e);
            throw new BusinessException("Erro ao listar workspaces");
        }
    }

    /**
     * Obter informações do workspace
     */
    public WorkspaceInfo getWorkspaceInfo(String serverId, String workspaceName) {
        try {
            Path workspacePath = Paths.get(basePath, serverId, "workspaces", workspaceName);

            if (!Files.exists(workspacePath)) {
                throw new ResourceNotFoundException("Workspace não encontrado");
            }

            return createWorkspaceInfo(workspacePath);

        } catch (Exception e) {
            log.error("Erro ao obter informações do workspace", e);
            throw new BusinessException("Erro ao obter informações do workspace");
        }
    }

    /**
     * Deletar workspace
     */
    public void deleteWorkspace(String serverId, String workspaceName) {
        try {
            Path workspacePath = Paths.get(basePath, serverId, "workspaces", workspaceName);

            if (!Files.exists(workspacePath)) {
                throw new ResourceNotFoundException("Workspace não encontrado");
            }

            fileSystemUtils.deleteDirectory(workspacePath);

            log.info("Workspace deletado: {} do servidor: {}", workspaceName, serverId);

        } catch (Exception e) {
            log.error("Erro ao deletar workspace", e);
            throw new BusinessException("Erro ao deletar workspace");
        }
    }

    /**
     * Listar arquivos do workspace
     */
    public List<FileInfo> listWorkspaceFiles(String serverId, String workspaceName) {
        try {
            Path workspacePath = Paths.get(basePath, serverId, "workspaces", workspaceName);

            if (!Files.exists(workspacePath)) {
                throw new ResourceNotFoundException("Workspace não encontrado");
            }

            List<FileInfo> files = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(workspacePath)) {
                for (Path path : stream) {
                    files.add(createFileInfo(path));
                }
            }

            return files;

        } catch (Exception e) {
            log.error("Erro ao listar arquivos do workspace", e);
            throw new BusinessException("Erro ao listar arquivos");
        }
    }

    /**
     * Obter path do workspace
     */
    public Path getWorkspacePath(String serverId, String workspaceName) {
        Path workspacePath = Paths.get(basePath, serverId, "workspaces", workspaceName);

        if (!Files.exists(workspacePath)) {
            throw new ResourceNotFoundException("Workspace não encontrado");
        }

        return workspacePath;
    }

    /**
     * Renomear workspace
     */
    public void renameWorkspace(String serverId, String oldName, String newName) {
        try {
            validateWorkspaceName(newName);

            Path oldPath = Paths.get(basePath, serverId, "workspaces", oldName);
            Path newPath = Paths.get(basePath, serverId, "workspaces", newName);

            if (!Files.exists(oldPath)) {
                throw new ResourceNotFoundException("Workspace não encontrado");
            }

            if (Files.exists(newPath)) {
                throw new BusinessException("Workspace com novo nome já existe");
            }

            Files.move(oldPath, newPath);

            log.info("Workspace renomeado: {} -> {} no servidor: {}", oldName, newName, serverId);

        } catch (Exception e) {
            log.error("Erro ao renomear workspace", e);
            throw new BusinessException("Erro ao renomear workspace");
        }
    }

    /**
     * Calcular tamanho do workspace
     */
    public long calculateWorkspaceSize(String serverId, String workspaceName) {
        try {
            Path workspacePath = Paths.get(basePath, serverId, "workspaces", workspaceName);

            if (!Files.exists(workspacePath)) {
                return 0L;
            }

            return Files.walk(workspacePath)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();

        } catch (Exception e) {
            log.error("Erro ao calcular tamanho do workspace", e);
            return 0L;
        }
    }

    // Métodos auxiliares privados

    private void validateWorkspaceName(String workspaceName) {
        if (workspaceName == null || workspaceName.trim().isEmpty()) {
            throw new BusinessException("Nome do workspace não pode ser vazio");
        }

        // Verificar caracteres inválidos
        if (!workspaceName.matches("^[a-zA-Z0-9_-]+$")) {
            throw new BusinessException("Nome do workspace contém caracteres inválidos");
        }

        if (workspaceName.length() > 50) {
            throw new BusinessException("Nome do workspace muito longo (máximo 50 caracteres)");
        }
    }

    private WorkspaceInfo createWorkspaceInfo(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        long size = calculateDirectorySize(path);
        int fileCount = countFiles(path);

        return WorkspaceInfo.builder()
                .name(path.getFileName().toString())
                .path(path.toString())
                .size(size)
                .fileCount(fileCount)
                .createdAt(attrs.creationTime().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
                .modifiedAt(attrs.lastModifiedTime().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
                .build();
    }

    private FileInfo createFileInfo(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        return FileInfo.builder()
                .name(path.getFileName().toString())
                .path(path.toString())
                .size(attrs.size())
                .isDirectory(attrs.isDirectory())
                .createdAt(attrs.creationTime().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
                .modifiedAt(attrs.lastModifiedTime().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
                .build();
    }

    private long calculateDirectorySize(Path directory) throws IOException {
        return Files.walk(directory)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0L;
                    }
                })
                .sum();
    }

    private int countFiles(Path directory) throws IOException {
        return (int) Files.walk(directory)
                .filter(Files::isRegularFile)
                .count();
    }
}