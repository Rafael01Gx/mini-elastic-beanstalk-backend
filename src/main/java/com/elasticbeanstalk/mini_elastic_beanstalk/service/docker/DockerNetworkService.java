package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.elasticbeanstalk.mini_elastic_beanstalk.exception.DockerOperationException;
import com.elasticbeanstalk.mini_elastic_beanstalk.util.NetworkUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Network;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DockerNetworkService {
    @Autowired
    private DockerClient dockerClient;
    @Autowired
    private NetworkUtils networkUtils;

    /**
     * Criar rede para servidor
     */
    public String createServerNetwork(String serverId) {
        try {
            String networkName = networkUtils.generateNetworkName(serverId);

            // Verificar se rede já existe
            List<Network> existingNetworks = dockerClient.listNetworksCmd()
                    .withNameFilter(networkName)
                    .exec();

            if (!existingNetworks.isEmpty()) {
                log.warn("Rede já existe: {}", networkName);
                return existingNetworks.get(0).getId();
            }

            // Criar nova rede
            CreateNetworkResponse response = dockerClient.createNetworkCmd()
                    .withName(networkName)
                    .withDriver("bridge")
                    .withLabels(Map.of("serverId", serverId))
                    .exec();

            log.info("Rede criada: {} para servidor: {}", networkName, serverId);

            return response.getId();

        } catch (Exception e) {
            log.error("Erro ao criar rede para servidor: {}", serverId, e);
            throw new DockerOperationException("Erro ao criar rede"+ e.getMessage());
        }
    }

    /**
     * Remover rede
     */
    public void removeNetwork(String networkId) {
        try {
            dockerClient.removeNetworkCmd(networkId).exec();
            log.info("Rede removida: {}", networkId);
        } catch (Exception e) {
            log.error("Erro ao remover rede: {}", networkId, e);
            throw new DockerOperationException("Erro ao remover rede"+ e.getMessage());
        }
    }

    /**
     * Conectar container à rede
     */
    public void connectContainer(String containerId, String networkId) {
        try {
            dockerClient.connectToNetworkCmd()
                    .withContainerId(containerId)
                    .withNetworkId(networkId)
                    .exec();

            log.info("Container {} conectado à rede {}", containerId, networkId);

        } catch (Exception e) {
            log.error("Erro ao conectar container à rede", e);
            throw new DockerOperationException("Erro ao conectar container à rede"+ e.getMessage());
        }
    }

    /**
     * Desconectar container da rede
     */
    public void disconnectContainer(String containerId, String networkId) {
        try {
            dockerClient.disconnectFromNetworkCmd()
                    .withContainerId(containerId)
                    .withNetworkId(networkId)
                    .exec();

            log.info("Container {} desconectado da rede {}", containerId, networkId);

        } catch (Exception e) {
            log.error("Erro ao desconectar container da rede", e);
            throw new DockerOperationException("Erro ao desconectar container da rede"+ e.getMessage());
        }
    }

    /**
     * Listar redes por servidor
     */
    public List<Network> listNetworksByServer(String serverId) {
        try {
            return dockerClient.listNetworksCmd()
                    .withFilter("label", List.of("serverId=" + serverId))
                    .exec();
        } catch (Exception e) {
            log.error("Erro ao listar redes do servidor: {}", serverId, e);
            throw new DockerOperationException("Erro ao listar redes"+ e.getMessage());
        }
    }

    /**
     * Inspecionar rede
     */
    public Network inspectNetwork(String networkId) {
        try {
            return dockerClient.inspectNetworkCmd()
                    .withNetworkId(networkId)
                    .exec();
        } catch (Exception e) {
            log.error("Erro ao inspecionar rede: {}", networkId, e);
            throw new DockerOperationException("Erro ao inspecionar rede"+ e.getMessage());
        }
    }

    /**
     * Listar todas as redes
     */
    public List<Network> listAllNetworks() {
        try {
            return dockerClient.listNetworksCmd().exec();
        } catch (Exception e) {
            log.error("Erro ao listar redes", e);
            throw new DockerOperationException("Erro ao listar redes"+ e.getMessage());
        }
    }
}