package com.elasticbeanstalk.mini_elastic_beanstalk.validator;

import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStreamReader;
import java.util.List;

@Component
@Slf4j
public class FileValidator {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    private static final List<String> DANGEROUS_EXTENSIONS = List.of(
            "exe", "bat", "cmd", "sh", "ps1", "vbs", "jar"
    );

    /**
     * Validar arquivo enviado
     */
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo não pode ser vazio");
        }

        validateFileName(file.getOriginalFilename());
        validateFileSize(file.getSize());
        validateFileExtension(file.getOriginalFilename());
    }

    /**
     * Validar nome do arquivo
     */
    private void validateFileName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new BusinessException("Nome do arquivo inválido");
        }

        // Verificar caracteres perigosos
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException("Nome do arquivo contém caracteres inválidos");
        }

        // Verificar tamanho do nome
        if (filename.length() > 255) {
            throw new BusinessException("Nome do arquivo muito longo");
        }
    }

    /**
     * Validar tamanho do arquivo
     */
    private void validateFileSize(long size) {
        long maxSize = parseSize(maxFileSize);

        if (size > maxSize) {
            throw new BusinessException(
                    String.format("Arquivo muito grande. Máximo permitido: %s", maxFileSize)
            );
        }

        if (size == 0) {
            throw new BusinessException("Arquivo está vazio");
        }
    }

    /**
     * Validar extensão do arquivo
     */
    private void validateFileExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Tipo de arquivo não permitido: " + extension);
        }
    }

    /**
     * Validar se é arquivo YAML válido
     */
    public void validateYamlFile(MultipartFile file) {
        validate(file);

        String filename = file.getOriginalFilename();
        if (!filename.endsWith(".yml") && !filename.endsWith(".yaml")) {
            throw new BusinessException("Arquivo deve ser YAML (.yml ou .yaml)");
        }

        try {
            Yaml yaml = new Yaml();
            yaml.load(new InputStreamReader(file.getInputStream()));
        } catch (Exception e) {
            throw new BusinessException("Arquivo YAML inválido: " + e.getMessage());
        }
    }

    /**
     * Validar se é arquivo .env válido
     */
    public void validateEnvFile(MultipartFile file) {
        validate(file);

        String filename = file.getOriginalFilename();
        if (!filename.equals(".env") && !filename.endsWith(".env")) {
            throw new BusinessException("Arquivo deve ser .env");
        }
    }

    // Métodos auxiliares

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    private long parseSize(String size) {
        size = size.toUpperCase().trim();
        long multiplier = 1;

        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        }

        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 100 * 1024 * 1024; // Default 100MB
        }
    }
}