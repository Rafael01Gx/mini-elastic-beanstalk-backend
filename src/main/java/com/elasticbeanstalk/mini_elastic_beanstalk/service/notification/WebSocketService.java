package com.elasticbeanstalk.mini_elastic_beanstalk.service.notification;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.LogMessage;
import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.MetricsResponse;
import com.elasticbeanstalk.mini_elastic_beanstalk.service.monitoring.MonitoringService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private DockerClient dockerClient;
    @Autowired
    private MonitoringService monitoringService;

    public void streamLogs(String containerId, String destination) {
        try {
            ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame frame) {
                    String log = new String(frame.getPayload());

                    LogMessage message = LogMessage.builder()
                            .containerId(containerId)
                            .timestamp(LocalDateTime.now())
                            .message(log)
                            .build();

                    messagingTemplate.convertAndSend(destination, message);
                }
            };

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTailAll()
                    .exec(callback);

        } catch (Exception e) {
            log.error("Erro ao fazer stream de logs", e);
        }
    }

    public void streamMetrics(String serverId, String destination) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                MetricsResponse metrics = monitoringService.getServerMetrics(serverId);
                messagingTemplate.convertAndSend(destination, metrics);
            } catch (Exception e) {
                log.error("Erro ao enviar métricas", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}