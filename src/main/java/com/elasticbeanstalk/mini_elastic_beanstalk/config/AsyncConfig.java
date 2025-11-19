package com.elasticbeanstalk.mini_elastic_beanstalk.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configurar executor de tarefas assíncronas
     * NOTA: ThreadPoolTaskExecutor é gerenciado pelo Spring e não precisa de try-with-resources
     * O Spring chama automaticamente destroy() no shutdown através do @Bean
     */
    @Override
    @Bean(name = "taskExecutor", destroyMethod = "shutdown")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Número de threads principais
        executor.setCorePoolSize(5);

        // Número máximo de threads
        executor.setMaxPoolSize(10);

        // Capacidade da fila
        executor.setQueueCapacity(100);

        // Prefixo dos nomes das threads
        executor.setThreadNamePrefix("Async-");

        // Política de rejeição quando fila está cheia
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Aguardar tarefas terminarem no shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Timeout para aguardar tarefas no shutdown (30 segundos)
        executor.setAwaitTerminationSeconds(30);

        // Inicializar o executor
        executor.initialize();

        log.info("ThreadPoolTaskExecutor 'taskExecutor' configurado: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Handler para exceções não capturadas em métodos assíncronos
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Exceção não capturada em método assíncrono: {}", method.getName());
            log.error("Mensagem: {}", throwable.getMessage());
            log.error("Parâmetros: {}", Arrays.toString(params));
            log.error("Stacktrace:", throwable);

            // Aqui você pode adicionar notificações, alertas, etc.
        };
    }

    /**
     * Executor para tarefas agendadas
     * O destroyMethod garante shutdown limpo
     */
    @Bean(name = "taskScheduler", destroyMethod = "shutdown")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("Scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.initialize();

        log.info("TaskScheduler configurado com pool size: {}", scheduler.getPoolSize());

        return scheduler;
    }

    /**
     * Executor específico para operações Docker (IO-bound)
     */
    @Bean(name = "dockerExecutor", destroyMethod = "shutdown")
    public Executor dockerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Docker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Docker Executor configurado: core={}, max={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * Executor para deploy de aplicações (tarefas longas)
     */
    @Bean(name = "deployExecutor", destroyMethod = "shutdown")
    public Executor deployExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Deploy-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // 2 minutos para deploys
        executor.initialize();

        log.info("Deploy Executor configurado: core={}, max={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * Executor para monitoramento (lightweight)
     */
    @Bean(name = "monitoringExecutor", destroyMethod = "shutdown")
    public Executor monitoringExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Monitoring-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();

        log.info("Monitoring Executor configurado: core={}, max={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }

    /**
     * Bean PreDestroy para garantir shutdown limpo de todos os executors
     * (Alternativa ao destroyMethod, mais explícito)
     */
    @PreDestroy
    public void cleanup() {
        log.info("Iniciando shutdown dos executors assíncronos...");
        // O Spring já chama os destroyMethods automaticamente
        // Este método é apenas para logging adicional se necessário
    }
}