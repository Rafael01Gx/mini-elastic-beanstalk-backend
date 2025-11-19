package com.elasticbeanstalk.mini_elastic_beanstalk.service.monitoring;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.HealthResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.MetricsResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.repository.ContainerRepository;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.InvocationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MonitoringService {

    @Autowired
    private DockerClient dockerClient;
    @Autowired
    private ContainerRepository containerRepository;

    public MetricsResponse getServerMetrics(String serverId) {
        List<Container> containers =
                dockerClient.listContainersCmd()
                        .withLabelFilter(Map.of("serverId", serverId))
                        .exec();

        double totalCpuUsage = 0;
        long totalMemoryUsage = 0;
        long totalMemoryLimit = 0;
        int runningContainers = 0;

        for (com.github.dockerjava.api.model.Container container : containers) {
            try {
               InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
                Statistics stats = dockerClient.statsCmd(container.getId())
                        .withNoStream(true)
                        .exec(callback)
                        .awaitResult();

                double cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                        stats.getPreCpuStats().getCpuUsage().getTotalUsage();
                double systemDelta = stats.getCpuStats().getSystemCpuUsage() -
                        stats.getPreCpuStats().getSystemCpuUsage();
                int cpuCount = Math.toIntExact(stats.getCpuStats().getOnlineCpus());

                double cpuPercent = (cpuDelta / systemDelta) * cpuCount * 100.0;
                totalCpuUsage += cpuPercent;

                long memUsage = stats.getMemoryStats().getUsage();
                long memLimit = stats.getMemoryStats().getLimit();
                totalMemoryUsage += memUsage;
                totalMemoryLimit += memLimit;

                if (container.getState().equals("running")) {
                    runningContainers++;
                }

            } catch (Exception e) {
                log.warn("Erro ao obter stats do container: {}", container.getId());
            }
        }

        return MetricsResponse.builder()
                .serverId(serverId)
                .totalContainers(containers.size())
                .runningContainers(runningContainers)
                .avgCpuUsage(containers.isEmpty() ? 0 : totalCpuUsage / containers.size())
                .totalMemoryUsageMB(totalMemoryUsage / (1024 * 1024))
                .totalMemoryLimitMB(totalMemoryLimit / (1024 * 1024))
                .timestamp(LocalDateTime.now())
                .build();
    }

    public HealthResponse checkServerHealth(String serverId) {
        List<com.github.dockerjava.api.model.Container> containers =
                dockerClient.listContainersCmd()
                        .withLabelFilter(Map.of("serverId", serverId))
                        .exec();

        int healthy = 0;
        int unhealthy = 0;
        int noHealthcheck = 0;

        for (com.github.dockerjava.api.model.Container container : containers) {
            InspectContainerResponse inspect = dockerClient
                    .inspectContainerCmd(container.getId())
                    .exec();

            if (inspect.getState().getHealth() != null) {
                String health = inspect.getState().getHealth().getStatus();
                if ("healthy".equals(health)) {
                    healthy++;
                } else {
                    unhealthy++;
                }
            } else {
                noHealthcheck++;
            }
        }

        return HealthResponse.builder()
                .serverId(serverId)
                .healthyContainers(healthy)
                .unhealthyContainers(unhealthy)
                .noHealthcheck(noHealthcheck)
                .overallStatus(unhealthy == 0 ? "HEALTHY" : "DEGRADED")
                .build();
    }
}