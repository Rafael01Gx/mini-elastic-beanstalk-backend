package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.elasticbeanstalk.mini_elastic_beanstalk.exception.DockerOperationException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListVolumesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DockerVolumeService {
    @Autowired
    private DockerClient dockerClient;

    /**
     * Criar volume
     */
    public String createVolume(String serverId, String volumeName) {
        try {
            CreateVolumeResponse response = dockerClient.createVolumeCmd()
                    .withName(volumeName)
                    .withLabels(Map.of("serverId", serverId))
                    .exec();

            log.info("Volume criado: {} para servidor: {}", volumeName, serverId);

            return response.getName();

        } catch (Exception e) {
            log.error("Erro ao criar volume", e);
            throw new DockerOperationException("Erro ao criar volume"+ e.getMessage());}
    }

    /**
     * Remover volume
     */
    public void removeVolume(String volumeName) {
        try {
            dockerClient.removeVolumeCmd(volumeName).exec();
            log.info("Volume removido: {}", volumeName);
        } catch (Exception e) {
            log.error("Erro ao remover volume: {}", volumeName, e);
            throw new DockerOperationException("Erro ao remover volume"+ e.getMessage());
        }
    }

    /**
     * Listar volumes por servidor
     */
    public List<InspectVolumeResponse> listVolumesByServer(String serverId) {
        try {
            ListVolumesResponse response = dockerClient.listVolumesCmd()
                    .withFilter("label", List.of("serverId=" + serverId))
                    .exec();

            return response.getVolumes();

        } catch (Exception e) {
            log.error("Erro ao listar volumes do servidor: {}", serverId, e);
            throw new DockerOperationException("Erro ao listar volumes"+ e.getMessage());
        }
    }

    /**
     * Inspecionar volume
     */
    public InspectVolumeResponse inspectVolume(String volumeName) {
        try {
            return dockerClient.inspectVolumeCmd(volumeName).exec();
        } catch (Exception e) {
            log.error("Erro ao inspecionar volume: {}", volumeName, e);
            throw new DockerOperationException("Erro ao inspecionar volume"+ e.getMessage());
        }
    }

    /**
     * Limpar volumes não utilizados do servidor
     */
    public void pruneServerVolumes(String serverId) {
        try {
            List<InspectVolumeResponse> volumes = listVolumesByServer(serverId);

            for (InspectVolumeResponse volume : volumes) {
                try {
                    removeVolume(volume.getName());
                } catch (Exception e) {
                    log.warn("Não foi possível remover volume: {}", volume.getName());
                }
            }

            log.info("Volumes limpos para servidor: {}", serverId);

        } catch (Exception e) {
            log.error("Erro ao limpar volumes do servidor: {}", serverId, e);
            throw new DockerOperationException("Erro ao limpar volumes"+ e.getMessage());
        }
    }
}