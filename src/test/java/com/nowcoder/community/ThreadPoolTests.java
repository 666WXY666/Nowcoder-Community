package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    // JDK线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK定时任务线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // Spring定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    // 封装sleep方法
    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // JDK线程池
    @Test
    public void testExecutorService() {
        Runnable task = () -> logger.debug("Hello ExecutorService");
        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void testScheduledExecutorService() {
        Runnable task = () -> logger.debug("Hello ScheduledExecutorService");
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    // Spring线程池
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = () -> logger.debug("Hello ThreadPoolTaskExecutor");
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = () -> logger.debug("Hello ThreadPoolTaskScheduler");
        taskScheduler.scheduleAtFixedRate(task, new Date(System.currentTimeMillis() + 10000).toInstant(), Duration.ofMillis(1000));
        sleep(30000);
    }

    // Async异步注解方式
    @Test
    public void testAsync() {
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }
        sleep(10000);
    }

    @Test
    public void testScheduled() {
        // @Scheduled注解的函数程序启动后会自动执行，不需要手动调用
        sleep(30000);
    }
}
