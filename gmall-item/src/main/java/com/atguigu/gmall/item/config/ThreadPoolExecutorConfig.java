package com.atguigu.gmall.item.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
@Slf4j
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${threadPool.corePoolSize}") Integer corePoolSize,
            @Value("${threadPool.maximumPoolSize}") Integer maximumPoolSize,
            @Value("${threadPool.keepAliveTime}") Long keepAliveTime,
            @Value("${threadPool.blockSize}") Integer blockSize) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockSize),Executors.defaultThreadFactory() ,(Runnable r, ThreadPoolExecutor executor) -> {
                    log.warn("执行了拒绝策略");
        });
    }
}
