package com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.FileInfo;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.ResourceNotFoundException;
import com.elasticbeanstalk.mini_elastic_beanstalk.validator.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FileUploadService {

    @Autowired
    private FileValidator fileValidator;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    /**
     * Salvar arquivo enviado
     */
    public Path saveFile(MultipartFile file, Path destination) {
        try {
            // Validar arquivo
            fileValidator.validate(file);

            // Criar diretório pai se não existir
            Files.createDirectories(destination.getParent());

            // Salvar arquivo
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Arquivo salvo: {}", destination);

            return destination;

        } catch (Exception e) {
            log.error("Erro ao salvar arquivo", e);
            throw new BusinessException("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    /**
     * Salvar múltiplos arquivos
     */
    public List<Path> saveFiles(List<MultipartFile> files, Path directory) {
        try {
            Files.createDirectories(directory);

            List<Path> savedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                Path destination = directory.resolve(file.getOriginalFilename());
                savedFiles.add(saveFile(file, destination));
            }

            return savedFiles;

        } catch (Exception e) {
            log.error("Erro ao salvar múltiplos arquivos", e);
            throw new BusinessException("Erro ao salvar arquivos");
        }
    }

    /**
     * Salvar e descompactar arquivo ZIP
     */
    public Path saveAndExtractZip(MultipartFile file, Path destination) {
        try {
            // Validar se é arquivo ZIP
            if (!file.getOriginalFilename().endsWith(".zip")) {
                throw new BusinessException("Arquivo deve ser um ZIP");
            }

            fileValidator.validate(file);

            // Criar diretório de destino
            Files.createDirectories(destination);

            // Extrair ZIP
            try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;

                while ((entry = zipInputStream.getNextEntry()) != null) {
                    Path entryPath = destination.resolve(entry.getName());

                    // Prevenir Path Traversal
                    if (!entryPath.startsWith(destination)) {
                        throw new BusinessException("Entrada ZIP inválida: " + entry.getName());
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    zipInputStream.closeEntry();
                }
            }

            log.info("Arquivo ZIP extraído: {}", destination);

            return destination;

        } catch (Exception e) {
            log.error("Erro ao extrair ZIP", e);
            throw new BusinessException("Erro ao extrair arquivo ZIP");
        }
    }

    /**
     * Salvar arquivo de texto
     */
    public Path saveTextFile(String content, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
            Files.writeString(destination, content, StandardCharsets.UTF_8);

            log.info("Arquivo de texto salvo: {}", destination);

            return destination;

        } catch (Exception e) {
            log.error("Erro ao salvar arquivo de texto", e);
            throw new BusinessException("Erro ao salvar arquivo de texto");
        }
    }

    /**
     * Ler arquivo de texto
     */
    public String readTextFile(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Arquivo não encontrado");
            }

            return Files.readString(filePath, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Erro ao ler arquivo de texto", e);
            throw new BusinessException("Erro ao ler arquivo");
        }
    }

    /**
     * Deletar arquivo
     */
    public void deleteFile(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Arquivo deletado: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Erro ao deletar arquivo", e);
            throw new BusinessException("Erro ao deletar arquivo");
        }
    }

    /**
     * Copiar arquivo
     */
    public Path copyFile(Path source, Path destination) {
        try {
            if (!Files.exists(source)) {
                throw new ResourceNotFoundException("Arquivo fonte não encontrado");
            }

            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

            log.info("Arquivo copiado: {} -> {}", source, destination);

            return destination;

        } catch (Exception e) {
            log.error("Erro ao copiar arquivo", e);
            throw new BusinessException("Erro ao copiar arquivo");
        }
    }

    /**
     * Mover arquivo
     */
    public Path moveFile(Path source, Path destination) {
        try {
            if (!Files.exists(source)) {
                throw new ResourceNotFoundException("Arquivo fonte não encontrado");
            }

            Files.createDirectories(destination.getParent());
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);

            log.info("Arquivo movido: {} -> {}", source, destination);

            return destination;

        } catch (Exception e) {
            log.error("Erro ao mover arquivo", e);
            throw new BusinessException("Erro ao mover arquivo");
        }
    }

    /**
     * Obter informações do arquivo
     */
    public FileInfo getFileInfo(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Arquivo não encontrado");
            }

            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

            return FileInfo.builder()
                    .name(filePath.getFileName().toString())
                    .path(filePath.toString())
                    .size(attrs.size())
                    .isDirectory(attrs.isDirectory())
                    .createdAt(attrs.creationTime().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
                    .modifiedAt(attrs.lastModifiedTime().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao obter informações do arquivo", e);
            throw new BusinessException("Erro ao obter informações do arquivo");
        }
    }

    /**
     * Validar tipo de arquivo por extensão
     */
    public boolean isAllowedFileType(String filename, List<String> allowedExtensions) {
        String extension = getFileExtension(filename);
        return allowedExtensions.stream()
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }

    /**
     * Obter extensão do arquivo
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}