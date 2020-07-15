package ru.mts.megogo.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Configuration
@Slf4j
public class ExecutorConfiguration {

    @Value("${thread.pool.size.parser}")
    private Integer threadPoolSizeParser;

    @Value("${buffer.catalog}")
    private Integer bufferCatalog;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForParser() {
        return createExecutor("Parser-", threadPoolSizeParser);
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForCatalog() {
        return createExecutor("Catalog-", 1);
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForGetPageMegogo() {
        return createExecutor("PageMegogo-", 1);
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForGetPageKinopoisk() {
        return createExecutor("PageKP-", 1);
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForWriteFile() {
        return createExecutor("Write-", 1);
    }

    private ThreadPoolTaskExecutor createExecutor(String prefix, Integer size) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setThreadNamePrefix(prefix);
        if(size == null || size < 1) {
            log.info("incorrect properties \"thread.pool.size\" fir {}: {}", prefix, size);
            threadPoolTaskExecutor.setCorePoolSize(1);
            threadPoolTaskExecutor.setMaxPoolSize(1);
            return threadPoolTaskExecutor;
        }
        threadPoolTaskExecutor.setCorePoolSize(size);
        threadPoolTaskExecutor.setMaxPoolSize(size);
        return threadPoolTaskExecutor;
    }

    @Bean
    public BlockingQueue<String> filmItemUrlQueue() {
        return new LinkedBlockingDeque<>(bufferCatalog);
    }
}
