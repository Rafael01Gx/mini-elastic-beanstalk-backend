package com.elasticbeanstalk.mini_elastic_beanstalk.service.deploy;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.DeployResult;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Container;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Deploy;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.entity.Server;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.ContainerStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.enums.DeployStatus;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.BusinessException;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ContainerRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.DeployRepository;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.docker.DockerComposeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DeployExecutionService {
    @Autowired
    private DeployRepository deployRepository;
    @Autowired
    private ContainerRepository containerRepository;
    @Autowired
    private DockerComposeService dockerComposeService;

    @Transactional
    protected void executeDeployment(Long deployId) {

        Deploy deploy = deployRepository.findById(deployId)
                .orElseThrow(() -> new BusinessException("Deploy não encontrado para execução"));

        Path composePath = Path.of(deploy.getComposePath());
        try {
            deploy.setStatus(DeployStatus.DEPLOYING);
            deployRepository.save(deploy);

            DeployResult result = dockerComposeService.deployCompose(
                    deploy,
                    composePath
            );


            if (result.success()) {
                deploy.setStatus(DeployStatus.SUCCESS);
                registerContainers(deploy.getServer(), deploy,result);
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

    private void registerContainers(Server server,Deploy deploy, DeployResult result) {
        List<String> cNames = result.names();
        List<String> containerIds = result.containers();

        if (cNames.size() != containerIds.size()) {
            throw new IllegalStateException("A quantidade de IDs e Nomes não coincide.");
        }

        for (int i = 0; i < containerIds.size(); i++) {
            Container container = Container.builder()
                    .server(server)
                    .id(containerIds.get(i))
                    .name(cNames.get(i).replaceAll("[/\\[\\]]", ""))
                    .image(cNames.get(i).replaceAll("[/\\[\\]]", ""))
                    .deploy(deploy)
                    .status(ContainerStatus.CREATED)
                    .build();

            containerRepository.save(container);
        }
    }

}
