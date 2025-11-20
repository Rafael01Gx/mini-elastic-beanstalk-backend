package com.elasticbeanstalk.mini_elastic_beanstalk.service.docker;

import com.elasticbeanstalk.mini_elastic_beanstalk.domain.dto.response.LogEntry;
import com.elasticbeanstalk.mini_elastic_beanstalk.exception.DockerOperationException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DockerLogService {

    @Autowired
    private DockerClient dockerClient;

    /**
     * Obter logs do container
     */
    public String getContainerLogs(String containerId, int tail) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(tail)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            try {
                                outputStream.write(frame.getPayload());
                            } catch (IOException e) {
                                log.error("Erro ao escrever log", e);
                            }
                        }
                    })
                    .awaitCompletion(10, TimeUnit.SECONDS);

            return outputStream.toString();

        } catch (Exception e) {
            log.error("Erro ao obter logs do container: {}", containerId, e);
            throw new DockerOperationException("Erro ao obter logs"+ e.getMessage());
        }
    }

    /**
     * Streaming de logs em tempo real
     */
    public void streamLogs(String containerId, LogStreamCallback callback) {
        try {
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTailAll()
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            String log = new String(frame.getPayload(), StandardCharsets.UTF_8);
                            callback.onLog(log);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            callback.onError(throwable.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            callback.onComplete();
                        }
                    });

        } catch (Exception e) {
            log.error("Erro ao fazer streaming de logs: {}", containerId, e);
            throw new DockerOperationException("Erro ao fazer streaming de logs"+ e.getMessage());
        }
    }

    /**
     * Obter logs com timestamp
     */
    public List<LogEntry> getContainerLogsWithTimestamp(String containerId, int tail) {
        try {
            List<LogEntry> logs = new ArrayList<>();

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTimestamps(true)
                    .withTail(tail)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            String logLine = new String(frame.getPayload(), StandardCharsets.UTF_8);
                            logs.add(parseLogEntry(logLine));
                        }
                    })
                    .awaitCompletion(10, TimeUnit.SECONDS);

            return logs;

        } catch (Exception e) {
            log.error("Erro ao obter logs com timestamp: {}", containerId, e);
            throw new DockerOperationException("Erro ao obter logs"+ e.getMessage());
        }
    }

    /**
     * Obter logs entre datas
     */
    public String getContainerLogsSince(String containerId, LocalDateTime since) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int sinceTimestamp = (int) since.toEpochSecond(ZoneOffset.UTC);

            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withSince(sinceTimestamp)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            try {
                                outputStream.write(frame.getPayload());
                            } catch (IOException e) {
                                log.error("Erro ao escrever log", e);
                            }
                        }
                    })
                    .awaitCompletion(10, TimeUnit.SECONDS);

            return outputStream.toString();

        } catch (Exception e) {
            log.error("Erro ao obter logs desde: {}", since, e);
            throw new DockerOperationException("Erro ao obter logs"+ e.getMessage());
        }
    }

    // Métodos auxiliares

    private LogEntry parseLogEntry(String logLine) {
        // Formato: 2024-01-01T10:00:00.000000000Z log message
        try {
            String[] parts = logLine.split(" ", 2);
            if (parts.length == 2) {
                return LogEntry.builder()
                        .timestamp(LocalDateTime.parse(parts[0], DateTimeFormatter.ISO_DATE_TIME))
                        .message(parts[1])
                        .build();
            }
        } catch (Exception e) {
            log.warn("Erro ao parsear linha de log: {}", logLine);
        }

        return LogEntry.builder()
                .timestamp(LocalDateTime.now())
                .message(logLine)
                .build();
    }
}

@FunctionalInterface
interface LogStreamCallback {
    void onLog(String log);

    default void onError(String error) {
        // Implementação padrão
    }

    default void onComplete() {
        // Implementação padrão
    }
}