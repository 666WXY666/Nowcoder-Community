package com.nowcoder.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class WkImageDeleteJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(WkImageDeleteJob.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        File[] files = new File(wkImageStorage).listFiles();
        if (files == null || files.length == 0) {
            logger.info("[任务取消] 没有需要删除的WK图片!");
            return;
        }
        logger.info("[任务开始] 正在删除WK图片: " + files.length + "个");
        for (File file : files) {
            // 删除一分钟之前创建的图片
            if (System.currentTimeMillis() - file.lastModified() > 60 * 1000) {
                logger.info("[任务执行] 删除WK图片: " + file.getName());
                if (!file.delete()) {
                    logger.error("[任务异常] 删除WK图片失败: " + file.getName());
                }
            }
        }
        logger.info("[任务结束] 删除WK图片完毕!");
    }
}
