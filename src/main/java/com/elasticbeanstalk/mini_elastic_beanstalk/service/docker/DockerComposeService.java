package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.ValidationResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.DockerOperationException;
import com.elasticbeanstalk.mini_elastic_beanstalk.util.YamlUtils;
import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DockerComposeService {

    private DockerClient dockerClient;
    private YamlUtils yamlUtils;

    public DeployResult deployCompose(String serverId, Path composePath) {
        try {
            String projectName = "app-" + serverId;
            Path workDir = composePath.getParent();

            ProcessBuilder pb = new ProcessBuilder(
                    "docker-compose",
                    "--project-name", projectName,
                    "--file", composePath.toString(),
                    "up", "-d"
            );

            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("Docker Compose: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                List<String> containerIds = getProjectContainers(projectName);

                return DeployResult.builder()
                        .success(true)
                        .containers(containerIds)
                        .output(output.toString())
                        .build();
            } else {
                return DeployResult.builder()
                        .success(false)
                        .errorMessage("Compose failed with exit code: " + exitCode)
                        .output(output.toString())
                        .build();
            }

        } catch (Exception e) {
            log.error("Erro ao executar docker-compose", e);
            return DeployResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    public void removeCompose(String serverId, String projectName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker-compose",
                    "--project-name", projectName,
                    "down",
                    "--volumes",
                    "--remove-orphans"
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.warn("Docker compose down retornou código: {}", exitCode);
            }

        } catch (Exception e) {
            log.error("Erro ao remover compose", e);
            throw new DockerOperationException("Erro ao remover compose :"+ e);
        }
    }

    public ValidationResult validateCompose(Path composePath) {
        try {
            Map<String, Object> yaml = yamlUtils.loadYaml(composePath);

            List<String> errors = new ArrayList<>();

            if (!yaml.containsKey("services")) {
                errors.add("Campo 'services' é obrigatório");
            }

            if (yaml.get("services") instanceof Map) {
                Map<String, Object> services = (Map<String, Object>) yaml.get("services");

                for (Map.Entry<String, Object> entry : services.entrySet()) {
                    String serviceName = entry.getKey();
                    Map<String, Object> service = (Map<String, Object>) entry.getValue();

                    if (!service.containsKey("image") && !service.containsKey("build")) {
                        errors.add("Service '" + serviceName + "' precisa de 'image' ou 'build'");
                    }
                }
            }

            return ValidationResult.builder()
                    .valid(errors.isEmpty())
                    .errors(errors)
                    .build();

        } catch (Exception e) {
            return ValidationResult.builder()
                    .valid(false)
                    .errors(List.of("Erro ao validar YAML: " + e.getMessage()))
                    .build();
        }
    }

    public void injectLabels(Path composePath, String serverId) {
        try {
            Map<String, Object> yaml = yamlUtils.loadYaml(composePath);

            if (yaml.get("services") instanceof Map) {
                Map<String, Object> services = (Map<String, Object>) yaml.get("services");

                for (Map.Entry<String, Object> entry : services.entrySet()) {
                    Map<String, Object> service = (Map<String, Object>) entry.getValue();

                    Map<String, String> labels = (Map<String, String>)
                            service.computeIfAbsent("labels", k -> new HashMap<>());

                    labels.put("serverId", serverId);

                    List<String> networks = (List<String>)
                            service.computeIfAbsent("networks", k -> new ArrayList<>());

                    String networkName = "network-server-" + serverId;
                    if (!networks.contains(networkName)) {
                        networks.add(networkName);
                    }
                }

                Map<String, Object> networks = (Map<String, Object>)
                        yaml.computeIfAbsent("networks", k -> new HashMap<>());

                String networkName = "network-server-" + serverId;
                networks.put(networkName, Map.of("external", true));
            }

            yamlUtils.saveYaml(yaml, composePath);

        } catch (Exception e) {
            log.error("Erro ao injetar labels", e);
            throw new BusinessException("Erro ao processar compose:" + e.getMessage());
        }
    }

    private List<String> getProjectContainers(String projectName) {
        try {
            List<com.github.dockerjava.api.model.Container> containers =
                    dockerClient.listContainersCmd()
                            .withLabelFilter(Map.of("com.docker.compose.project", projectName))
                            .exec();

            return containers.stream()
                    .map(com.github.dockerjava.api.model.Container::getId)
                    .toList();

        } catch (Exception e) {
            log.error("Erro ao listar containers do projeto", e);
            return List.of();
        }
    }
}